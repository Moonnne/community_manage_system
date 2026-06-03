package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.BuildingAddDTO;
import edu.tut.cms.pojo.dto.BuildingUpdateDTO;
import edu.tut.cms.pojo.vo.BuildingVO;
import edu.tut.cms.server.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping("/add")
    @RequireRole({0, 1})
    public Result<Void> add(@Valid @RequestBody BuildingAddDTO dto) {
        buildingService.addBuilding(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1})
    public Result<Void> update(@Valid @RequestBody BuildingUpdateDTO dto) {
        buildingService.updateBuilding(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1})
    public Result<Void> delete(@RequestParam Long id) {
        buildingService.deleteBuilding(id);
        return Result.success();
    }

    @GetMapping("/page")
    @RequireRole({0, 1})
    public Result<PageResult<BuildingVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String buildingCode,
                                               @RequestParam(required = false) String buildingName,
                                               @RequestParam(required = false) String buildingLocation,
                                               @RequestParam(required = false) String buildingRemark) {
        return Result.success(buildingService.pageBuildings(pageNum, pageSize, buildingCode, buildingName, buildingLocation, buildingRemark));
    }

    @GetMapping("/list")
    public Result<List<BuildingVO>> list() {
        return Result.success(buildingService.listBuildings());
    }
}
