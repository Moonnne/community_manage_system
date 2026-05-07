package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.CarAddDTO;
import edu.tut.cms.pojo.dto.CarUpdateDTO;
import edu.tut.cms.pojo.vo.CarTypeVO;
import edu.tut.cms.pojo.vo.CarVO;
import edu.tut.cms.server.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping("/add")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> add(@Valid @RequestBody CarAddDTO dto) {
        carService.addCar(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> update(@Valid @RequestBody CarUpdateDTO dto) {
        carService.updateCar(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> delete(@RequestParam Long id) {
        carService.deleteCar(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<CarVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) Long ownerId,
                                          @RequestParam(required = false) Integer carType,
                                          @RequestParam(required = false) Integer carStatus) {
        return Result.success(carService.pageCars(pageNum, pageSize, ownerId, carType, carStatus));
    }

    @GetMapping("/types")
    public Result<List<CarTypeVO>> types() {
        return Result.success(carService.listCarTypes());
    }
}
