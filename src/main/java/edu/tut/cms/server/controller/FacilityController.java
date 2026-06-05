package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.FacilityAddDTO;
import edu.tut.cms.pojo.dto.FacilityTypeAddDTO;
import edu.tut.cms.pojo.dto.FacilityUpdateDTO;
import edu.tut.cms.pojo.vo.FacilityTypeVO;
import edu.tut.cms.pojo.vo.FacilityVO;
import edu.tut.cms.server.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping("/add")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> add(@Valid @RequestBody FacilityAddDTO dto) {
        facilityService.addFacility(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> update(@Valid @RequestBody FacilityUpdateDTO dto) {
        facilityService.updateFacility(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> delete(@RequestParam Long id) {
        facilityService.deleteFacility(id);
        return Result.success();
    }

    @GetMapping("/page")
    @RequireRole({0, 1, 2})
    public Result<PageResult<FacilityVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Integer facilityType,
                                               @RequestParam(required = false) Integer facilityStatus,
                                               @RequestParam(required = false) String facilityCode,
                                               @RequestParam(required = false) String facilityName,
                                               @RequestParam(required = false) Long facilityBuilding) {
        return Result.success(facilityService.pageFacilities(pageNum, pageSize, facilityType, facilityStatus,
                facilityCode, facilityName, facilityBuilding));
    }

    @GetMapping("/near-expiration")
    @RequireRole({0, 1, 2})
    public Result<List<FacilityVO>> nearExpiration(@RequestParam(defaultValue = "3") int months) {
        return Result.success(facilityService.getNearExpirationFacilities(months));
    }

    @GetMapping("/types")
    @RequireRole({0, 1, 2})
    public Result<List<FacilityTypeVO>> types() {
        return Result.success(facilityService.listFacilityTypes());
    }

    @PostMapping("/type/add")
    @RequireRole({0, 1})
    public Result<Void> addType(@Valid @RequestBody FacilityTypeAddDTO dto) {
        facilityService.addFacilityType(dto);
        return Result.success();
    }

    @PostMapping("/type/delete")
    @RequireRole({0, 1})
    public Result<Void> deleteType(@RequestParam Integer id) {
        facilityService.deleteFacilityType(id);
        return Result.success();
    }
}
