package edu.tut.cms.server.controller;

import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.vo.AssetVO;
import edu.tut.cms.pojo.vo.CarHistoryVO;
import edu.tut.cms.pojo.vo.FacilityVO;
import edu.tut.cms.pojo.vo.TaskVO;
import edu.tut.cms.pojo.vo.VisitorVO;
import edu.tut.cms.server.mapper.AssetMapper;
import edu.tut.cms.server.mapper.CarHistoryMapper;
import edu.tut.cms.server.mapper.FacilityMapper;
import edu.tut.cms.server.mapper.TaskMapper;
import edu.tut.cms.server.mapper.VisitorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:00");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final VisitorMapper visitorMapper;
    private final CarHistoryMapper carHistoryMapper;
    private final FacilityMapper facilityMapper;
    private final AssetMapper assetMapper;
    private final TaskMapper taskMapper;

    @GetMapping("/security")
    public Result<Map<String, Object>> security(@RequestParam(defaultValue = "1d") String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart;
        LocalDateTime rangeStart = resolveRangeStart(range, now);

        List<VisitorVO> visitors = visitorMapper.selectPage(null, null, null, null, null, null, null, null);
        List<CarHistoryVO> histories = carHistoryMapper.selectPage(null, null, null, null, null);

        long todayVisitors = visitors.stream().filter(item -> inRange(item.getVisitorCreateTime(), todayStart, now)).count();
        long yesterdayVisitors = visitors.stream().filter(item -> inRange(item.getVisitorCreateTime(), yesterdayStart, yesterdayEnd)).count();
        long stayingVisitors = visitors.stream().filter(item -> "2".equals(item.getVisitorStatus())).count();
        long todayCars = histories.stream().filter(item -> inRange(item.getCarHistoryTime(), todayStart, now)).count();
        long yesterdayCars = histories.stream().filter(item -> inRange(item.getCarHistoryTime(), yesterdayStart, yesterdayEnd)).count();

        Map<String, Object> data = new HashMap<>();
        data.put("todayVisitorCount", todayVisitors);
        data.put("stayingVisitorCount", stayingVisitors);
        data.put("carFlowCount", todayCars);
        data.put("visitorChange", percentChange(todayVisitors, yesterdayVisitors));
        data.put("carChange", percentChange(todayCars, yesterdayCars));
        data.put("flow", buildFlow(rangeStart, now, visitors, histories));
        return Result.success(data);
    }

    @GetMapping("/facility")
    public Result<Map<String, Object>> facility() {
        List<FacilityVO> facilities = facilityMapper.selectPage(null, null, null, null, null);
        LocalDateTime now = LocalDateTime.now();

        long nearInspection = facilities.stream().filter(item -> {
            LocalDateTime due = facilityDueTime(item);
            return due != null && !due.isBefore(now) && !due.isAfter(now.plusMonths(1));
        }).count();
        long needInspection = facilities.stream().filter(item -> {
            LocalDateTime due = facilityDueTime(item);
            return due != null && due.isBefore(now);
        }).count();
        long abnormal = facilities.stream()
                .filter(item -> item.getFacilityStatus() != null && item.getFacilityStatus() != 0)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("registeredCount", facilities.size());
        data.put("nearInspectionCount", nearInspection);
        data.put("needInspectionCount", needInspection);
        data.put("abnormalCount", abnormal);
        return Result.success(data);
    }

    @GetMapping("/asset")
    public Result<Map<String, Object>> asset(@RequestParam(required = false) Long ownerId,
                                             @RequestParam(required = false) String assetName) {
        CurrentUser user = UserContext.getCurrentUser();
        List<AssetVO> assets = assetMapper.selectPage(user.getId(), null, null, null, null, null, null, null).stream()
                .filter(item -> !hasText(assetName) || contains(item.getAssetName(), assetName))
                .toList();
        LocalDateTime now = LocalDateTime.now();
        long expiring = assets.stream()
                .filter(item -> item.getAssetDueTime() != null
                        && !item.getAssetDueTime().isBefore(now)
                        && !item.getAssetDueTime().isAfter(now.plusMonths(1)))
                .count();
        long expired = assets.stream()
                .filter(item -> item.getAssetDueTime() != null && item.getAssetDueTime().isBefore(now))
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("assetCount", assets.size());
        data.put("expiringCount", expiring);
        data.put("expiredCount", expired);
        data.put("assets", assets);
        return Result.success(data);
    }

    @GetMapping("/task")
    public Result<Map<String, Object>> task(@RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer taskType,
                                            @RequestParam(required = false) Long ownerId) {
        CurrentUser user = UserContext.getCurrentUser();
        boolean canProcess = user.getUserRole() != null && (user.getUserRole() == 0 || user.getUserRole() == 1);

        List<TaskVO> myTasks = taskMapper.selectPage(user.getId(), null, null, null, null, false).stream()
                .filter(item -> !hasText(title) || contains(item.getTaskTitle(), title))
                .filter(item -> taskType == null || Objects.equals(item.getTaskType(), taskType))
                .toList();
        List<TaskVO> processingTasks = canProcess
                ? taskMapper.selectPage(ownerId, null, null, null, taskType, false).stream()
                    .filter(item -> !hasText(title) || contains(item.getTaskTitle(), title))
                    .filter(item -> Objects.equals(item.getTaskStatus(), 1) || Objects.equals(item.getTaskStatus(), 2))
                    .toList()
                : List.of();

        Map<String, Object> data = new HashMap<>();
        data.put("myTasks", myTasks);
        data.put("processingTasks", processingTasks);
        data.put("myCount", myTasks.size());
        data.put("pendingProcessCount", processingTasks.stream().filter(item -> !Objects.equals(item.getTaskStatus(), 0)).count());
        data.put("canProcess", canProcess);
        return Result.success(data);
    }

    private List<Map<String, Object>> buildFlow(LocalDateTime start,
                                                LocalDateTime end,
                                                List<VisitorVO> visitors,
                                                List<CarHistoryVO> histories) {
        boolean dayBucket = Duration.between(start, end).toHours() > 48;
        List<Map<String, Object>> points = new ArrayList<>();
        LocalDateTime cursor = dayBucket ? start.toLocalDate().atStartOfDay() : start.withMinute(0).withSecond(0).withNano(0);
        while (cursor.isBefore(end)) {
            LocalDateTime next = dayBucket ? cursor.plusDays(1) : cursor.plusHours(1);
            LocalDateTime bucketStart = cursor;
            LocalDateTime bucketEnd = next;
            long visitorCount = visitors.stream().filter(item -> inRange(item.getVisitorCreateTime(), bucketStart, bucketEnd)).count();
            long carCount = histories.stream().filter(item -> inRange(item.getCarHistoryTime(), bucketStart, bucketEnd)).count();
            Map<String, Object> point = new HashMap<>();
            point.put("label", cursor.format(dayBucket ? DAY_FORMATTER : HOUR_FORMATTER));
            point.put("visitorCount", visitorCount);
            point.put("carCount", carCount);
            points.add(point);
            cursor = next;
        }
        return points;
    }

    private LocalDateTime resolveRangeStart(String range, LocalDateTime now) {
        return switch (range) {
            case "1h" -> now.minusHours(1);
            case "3h" -> now.minusHours(3);
            case "6h" -> now.minusHours(6);
            case "12h" -> now.minusHours(12);
            case "3d" -> now.minusDays(3);
            default -> now.minusDays(1);
        };
    }

    private boolean inRange(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        return value != null && !value.isBefore(start) && value.isBefore(end);
    }

    private long percentChange(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }
        return Math.round((current - previous) * 100.0 / previous);
    }

    private LocalDateTime facilityDueTime(FacilityVO facility) {
        if (facility.getFacilityCheckTime() == null || facility.getFacilityExpirationDate() == null) {
            return null;
        }
        return facility.getFacilityCheckTime().plusMonths(facility.getFacilityExpirationDate());
    }

    private boolean contains(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
