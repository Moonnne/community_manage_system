package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.TaskAddDTO;
import edu.tut.cms.pojo.dto.TaskTypeAddDTO;
import edu.tut.cms.pojo.dto.TaskUpdateDTO;
import edu.tut.cms.pojo.entity.Task;
import edu.tut.cms.pojo.entity.TaskType;
import edu.tut.cms.pojo.vo.TaskTypeVO;
import edu.tut.cms.pojo.vo.TaskVO;
import edu.tut.cms.server.mapper.TaskMapper;
import edu.tut.cms.server.mapper.TaskTypeMapper;
import edu.tut.cms.server.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskTypeMapper taskTypeMapper;
    private static final DateTimeFormatter TASK_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTask(TaskAddDTO dto) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        String taskCode = generateTaskCode();

        Task task = Task.builder()
                .taskCode(taskCode)
                .taskTitle(dto.getTaskTitle())
                .taskInfo(dto.getTaskInfo())
                .ownerId(currentUser.getId())
                .taskStatus(1)
                .taskType(dto.getTaskType())
                .taskCreateTime(LocalDateTime.now())
                .taskRemark(null)
                .build();
        taskMapper.insert(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(TaskUpdateDTO dto) {
        Task existing = taskMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("工单不存在");
        }
        Task task = Task.builder()
                .id(dto.getId())
                .taskTitle(dto.getTaskTitle())
                .taskInfo(dto.getTaskInfo())
                .taskType(dto.getTaskType())
                .taskStatus(dto.getTaskStatus())
                .taskOperatorId(dto.getTaskOperatorId())
                .taskUpdateTime(LocalDateTime.now())
                .taskRemark(dto.getTaskRemark())
                .build();
        if (shouldSetFinishTime(existing, dto.getTaskStatus())) {
            task.setTaskFinishTime(LocalDateTime.now());
        }
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        taskMapper.deleteById(id);
    }

    @Override
    public PageResult<TaskVO> pageTasks(Integer pageNum, Integer pageSize,
                                        Long ownerId, Long taskOperatorId,
                                        String taskCode, Integer taskStatus, Integer taskType,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        Boolean excludeComplaint = currentUser.getUserRole() != null
                && (currentUser.getUserRole() == 2 || currentUser.getUserRole() == 3)
                && !currentUser.getId().equals(ownerId);
        List<TaskVO> list = taskMapper.selectPage(ownerId, taskOperatorId, taskCode, taskStatus, taskType, rangeStart, rangeEnd, excludeComplaint);
        PageInfo<TaskVO> pageInfo = new PageInfo<>(list);

        PageResult<TaskVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTask(Long id, Integer status, Long operatorId, String remark) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        Task existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("工单不存在");
        }
        if (existing.getTaskType() != null && existing.getTaskType() == 1
                && (currentUser.getUserRole() == null || (currentUser.getUserRole() != 0 && currentUser.getUserRole() != 1))) {
            throw new BusinessException("投诉/申诉类工单仅系统管理员和物业经理可处理");
        }
        Task task = Task.builder()
                .id(id)
                .taskOperatorId(operatorId)
                .taskStatus(status)
                .taskUpdateTime(LocalDateTime.now())
                .taskRemark(remark)
                .build();
        if (shouldSetFinishTime(existing, status)) {
            task.setTaskFinishTime(LocalDateTime.now());
        }
        taskMapper.updateById(task);
    }

    @Override
    public List<TaskTypeVO> listTaskTypes() {
        List<TaskType> types = taskTypeMapper.selectAll();
        return types.stream().map(t -> TaskTypeVO.builder()
                .taskTypeId(t.getTaskTypeId())
                .taskTypeName(t.getTaskTypeName())
                .taskTypeRemark(t.getTaskTypeRemark())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTaskType(TaskTypeAddDTO dto) {
        Integer nextTypeId = taskTypeMapper.selectMaxTypeId() + 1;
        TaskType type = TaskType.builder()
                .taskTypeId(nextTypeId)
                .taskTypeName(dto.getTaskTypeName())
                .taskTypeRemark(dto.getTaskTypeRemark())
                .build();
        taskTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskType(Integer id) {
        if (taskMapper.countByTaskTypeId(id) > 0) {
            throw new BusinessException("该工单类型已被工单使用，无法删除");
        }
        taskTypeMapper.deleteById(id);
    }

    private String generateTaskCode() {
        String prefix = "T" + LocalDateTime.now().format(TASK_CODE_FORMATTER);
        for (int i = 0; i < 20; i++) {
            String code = prefix + String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
            if (taskMapper.countByCode(code) == 0) {
                return code;
            }
        }
        throw new BusinessException("工单编号生成失败，请重试");
    }

    private boolean isFinishedStatus(Integer status) {
        return status != null && (status == 0 || status == 3 || status == 4);
    }

    private boolean shouldSetFinishTime(Task existing, Integer targetStatus) {
        if (!isFinishedStatus(targetStatus)) {
            return false;
        }
        return !isFinishedStatus(existing.getTaskStatus()) || existing.getTaskFinishTime() == null;
    }
}
