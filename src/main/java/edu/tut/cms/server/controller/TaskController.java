package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.TaskAddDTO;
import edu.tut.cms.pojo.dto.TaskTypeAddDTO;
import edu.tut.cms.pojo.dto.TaskUpdateDTO;
import edu.tut.cms.pojo.vo.TaskTypeVO;
import edu.tut.cms.pojo.vo.TaskVO;
import edu.tut.cms.server.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody TaskAddDTO dto) {
        taskService.addTask(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequirePermission(minLevel = 1)
    public Result<Void> update(@Valid @RequestBody TaskUpdateDTO dto) {
        taskService.updateTask(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequirePermission(minLevel = 1)
    public Result<Void> delete(@RequestParam Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<TaskVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) Long ownerId,
                                           @RequestParam(required = false) Long taskOperatorId,
                                           @RequestParam(required = false) String taskCode,
                                           @RequestParam(required = false) Integer taskStatus,
                                           @RequestParam(required = false) Integer taskType) {
        return Result.success(taskService.pageTasks(pageNum, pageSize, ownerId, taskOperatorId, taskCode, taskStatus, taskType));
    }

    @PostMapping("/process")
    @RequirePermission(minLevel = 1)
    public Result<Void> process(@RequestParam Long id,
                                @RequestParam Integer status,
                                @RequestParam(required = false) Long operatorId,
                                @RequestParam(required = false) String remark) {
        taskService.processTask(id, status, operatorId, remark);
        return Result.success();
    }

    @GetMapping("/types")
    public Result<List<TaskTypeVO>> types() {
        return Result.success(taskService.listTaskTypes());
    }

    @PostMapping("/type/add")
    @RequireRole({0, 1})
    public Result<Void> addType(@Valid @RequestBody TaskTypeAddDTO dto) {
        taskService.addTaskType(dto);
        return Result.success();
    }

    @PostMapping("/type/delete")
    @RequireRole({0, 1})
    public Result<Void> deleteType(@RequestParam Integer id) {
        taskService.deleteTaskType(id);
        return Result.success();
    }
}
