package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.CarHistoryAddDTO;
import edu.tut.cms.pojo.dto.CarHistoryBatchDeleteDTO;
import edu.tut.cms.pojo.dto.CarHistoryUpdateDTO;
import edu.tut.cms.pojo.vo.CarHistoryVO;
import edu.tut.cms.server.service.CarHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/car-history")
@RequiredArgsConstructor
public class CarHistoryController {

    private final CarHistoryService carHistoryService;

    @PostMapping("/add")
    @RequireRole({0, 1, 3})
    public Result<Void> add(@Valid @RequestBody CarHistoryAddDTO dto) {
        carHistoryService.addCarHistory(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1, 3})
    public Result<Void> update(@Valid @RequestBody CarHistoryUpdateDTO dto) {
        carHistoryService.updateCarHistory(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1, 3})
    public Result<Void> delete(@RequestParam Long id) {
        carHistoryService.deleteCarHistory(id);
        return Result.success();
    }

    @PostMapping("/delete/batch")
    @RequireRole({0, 1, 3})
    public Result<Void> batchDelete(@Valid @RequestBody CarHistoryBatchDeleteDTO dto) {
        carHistoryService.batchDeleteCarHistory(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @RequireRole({0, 1, 3})
    public Result<PageResult<CarHistoryVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) String carNumber,
                                                 @RequestParam(required = false) Integer gateId,
                                                 @RequestParam(required = false) Integer carHistoryType,
                                                 @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime rangeStart,
                                                 @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime rangeEnd) {
        return Result.success(carHistoryService.pageCarHistory(pageNum, pageSize, carNumber, gateId, carHistoryType, rangeStart, rangeEnd));
    }
}
