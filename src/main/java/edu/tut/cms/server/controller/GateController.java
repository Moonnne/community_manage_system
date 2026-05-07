package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.GateAddDTO;
import edu.tut.cms.pojo.dto.GateUpdateDTO;
import edu.tut.cms.pojo.vo.GateVO;
import edu.tut.cms.server.service.GateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gate")
@RequiredArgsConstructor
public class GateController {

    private final GateService gateService;

    @PostMapping("/add")
    @RequireRole({0, 1})
    public Result<Void> add(@Valid @RequestBody GateAddDTO dto) {
        gateService.addGate(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1})
    public Result<Void> update(@Valid @RequestBody GateUpdateDTO dto) {
        gateService.updateGate(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1})
    public Result<Void> delete(@RequestParam Integer id) {
        gateService.deleteGate(id);
        return Result.success();
    }

    @GetMapping("/list")
    @RequireRole({0, 1, 2, 3})
    public Result<List<GateVO>> list() {
        return Result.success(gateService.listGates());
    }
}
