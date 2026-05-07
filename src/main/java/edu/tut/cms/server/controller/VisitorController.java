package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.VisitorAddDTO;
import edu.tut.cms.pojo.dto.VisitorTypeAddDTO;
import edu.tut.cms.pojo.dto.VisitorUpdateDTO;
import edu.tut.cms.pojo.vo.VisitorTypeVO;
import edu.tut.cms.pojo.vo.VisitorVO;
import edu.tut.cms.server.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping("/add")
    @RequireRole({0, 1, 3})
    public Result<Void> add(@Valid @RequestBody VisitorAddDTO dto) {
        visitorService.addVisitor(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1, 3})
    public Result<Void> update(@Valid @RequestBody VisitorUpdateDTO dto) {
        visitorService.updateVisitor(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1, 3})
    public Result<Void> delete(@RequestParam Long id) {
        visitorService.deleteVisitor(id);
        return Result.success();
    }

    @GetMapping("/page")
    @RequireRole({0, 1, 3})
    public Result<PageResult<VisitorVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Long ownerId,
                                              @RequestParam(required = false) Integer visitorType,
                                              @RequestParam(required = false) String visitorStatus,
                                              @RequestParam(required = false) String visitorName) {
        return Result.success(visitorService.pageVisitors(pageNum, pageSize, ownerId, visitorType, visitorStatus, visitorName));
    }

    @GetMapping("/types")
    @RequireRole({0, 1, 3})
    public Result<List<VisitorTypeVO>> types() {
        return Result.success(visitorService.listVisitorTypes());
    }

    @PostMapping("/type/add")
    @RequireRole({0, 1})
    public Result<Void> addType(@Valid @RequestBody VisitorTypeAddDTO dto) {
        visitorService.addVisitorType(dto);
        return Result.success();
    }

    @PostMapping("/type/delete")
    @RequireRole({0, 1})
    public Result<Void> deleteType(@RequestParam Integer id) {
        visitorService.deleteVisitorType(id);
        return Result.success();
    }
}
