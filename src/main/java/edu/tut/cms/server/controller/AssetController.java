package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.AssetAddDTO;
import edu.tut.cms.pojo.dto.AssetUpdateDTO;
import edu.tut.cms.pojo.vo.AssetTypeVO;
import edu.tut.cms.pojo.vo.AssetVO;
import edu.tut.cms.server.service.AssetService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping("/add")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> add(@Valid @RequestBody AssetAddDTO dto) {
        assetService.addAsset(dto);
        return Result.success();
    }

    @PostMapping("/update")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> update(@Valid @RequestBody AssetUpdateDTO dto) {
        assetService.updateAsset(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0, 1, 2})
    @RequirePermission(minLevel = 1)
    public Result<Void> delete(@RequestParam Long id) {
        assetService.deleteAsset(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<AssetVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) Long assetOwnerId,
                                            @RequestParam(required = false) Integer assetType,
                                            @RequestParam(required = false) Integer assetStatus,
                                            @RequestParam(required = false) String assetBuilding) {
        return Result.success(assetService.pageAssets(pageNum, pageSize, assetOwnerId, assetType, assetStatus, assetBuilding));
    }

    @GetMapping("/export")
    @RequirePermission(minLevel = 1)
    public void exportExcel(@RequestParam(required = false) Long assetOwnerId,
                            HttpServletResponse response) throws IOException {
        byte[] bytes = assetService.exportAssetsToExcel(assetOwnerId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("资产导出.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/types")
    public Result<List<AssetTypeVO>> types() {
        return Result.success(assetService.listAssetTypes());
    }
}
