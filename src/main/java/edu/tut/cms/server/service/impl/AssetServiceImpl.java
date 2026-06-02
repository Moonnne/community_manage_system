package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.AssetAddDTO;
import edu.tut.cms.pojo.dto.AssetUpdateDTO;
import edu.tut.cms.pojo.entity.Asset;
import edu.tut.cms.pojo.entity.AssetType;
import edu.tut.cms.pojo.vo.AssetTypeVO;
import edu.tut.cms.pojo.vo.AssetVO;
import edu.tut.cms.server.mapper.AssetMapper;
import edu.tut.cms.server.mapper.AssetTypeMapper;
import edu.tut.cms.server.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetTypeMapper assetTypeMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAsset(AssetAddDTO dto) {
        Asset asset = Asset.builder()
                .assetCode(dto.getAssetCode())
                .assetOwnerId(dto.getAssetOwnerId())
                .assetName(dto.getAssetName())
                .assetType(dto.getAssetType())
                .assetRegTime(LocalDateTime.now())
                .assetDueTime(dto.getAssetDueTime())
                .assetStatus(dto.getAssetStatus())
                .assetArea(dto.getAssetArea())
                .assetBuilding(dto.getAssetBuilding())
                .assetFloor(dto.getAssetFloor())
                .assetPosition(dto.getAssetPosition())
                .assetRemark(dto.getAssetRemark())
                .build();
        assetMapper.insert(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAsset(AssetUpdateDTO dto) {
        Asset asset = Asset.builder()
                .id(dto.getId())
                .assetOwnerId(dto.getAssetOwnerId())
                .assetName(dto.getAssetName())
                .assetType(dto.getAssetType())
                .assetDueTime(dto.getAssetDueTime())
                .assetStatus(dto.getAssetStatus())
                .assetArea(dto.getAssetArea())
                .assetBuilding(dto.getAssetBuilding())
                .assetFloor(dto.getAssetFloor())
                .assetPosition(dto.getAssetPosition())
                .assetRemark(dto.getAssetRemark())
                .build();
        assetMapper.updateById(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(Long id) {
        assetMapper.deleteById(id);
    }

    @Override
    public PageResult<AssetVO> pageAssets(Integer pageNum, Integer pageSize,
                                          Long assetOwnerId, Integer assetType,
                                          Integer assetStatus, String assetBuilding) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            assetOwnerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        List<AssetVO> list = assetMapper.selectPage(assetOwnerId, assetType, assetStatus, assetBuilding);
        PageInfo<AssetVO> pageInfo = new PageInfo<>(list);

        PageResult<AssetVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAssetsToExcel(Long assetOwnerId) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            assetOwnerId = currentUser.getId();
        }

        List<AssetVO> assets = assetMapper.selectAll(assetOwnerId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("资产数据");

            XSSFRow header = sheet.createRow(0);
            String[] columns = {"资产编码", "业主", "资产名称", "资产类型", "登记时间", "到期时间", "状态", "面积", "楼栋", "楼层", "位置", "备注"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            for (int i = 0; i < assets.size(); i++) {
                AssetVO asset = assets.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(asset.getAssetCode() != null ? asset.getAssetCode() : "");
                row.createCell(1).setCellValue(asset.getOwnerName() != null ? asset.getOwnerName() : "");
                row.createCell(2).setCellValue(asset.getAssetName() != null ? asset.getAssetName() : "");
                row.createCell(3).setCellValue(asset.getAssetTypeName() != null ? asset.getAssetTypeName() : "");
                row.createCell(4).setCellValue(asset.getAssetRegTime() != null ? asset.getAssetRegTime().format(FORMATTER) : "");
                row.createCell(5).setCellValue(asset.getAssetDueTime() != null ? asset.getAssetDueTime().format(FORMATTER) : "");
                row.createCell(6).setCellValue(getAssetStatusName(asset.getAssetStatus()));
                row.createCell(7).setCellValue(asset.getAssetArea() != null ? asset.getAssetArea().doubleValue() : 0);
                row.createCell(8).setCellValue(asset.getAssetBuilding() != null ? asset.getAssetBuilding() : "");
                row.createCell(9).setCellValue(asset.getAssetFloor() != null ? asset.getAssetFloor() : "");
                row.createCell(10).setCellValue(asset.getAssetPosition() != null ? asset.getAssetPosition() : "");
                row.createCell(11).setCellValue(asset.getAssetRemark() != null ? asset.getAssetRemark() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出资产Excel失败", e);
            throw new BusinessException("导出失败");
        }
    }

    private String getAssetStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "过期";
            case 2 -> "未使用";
            case 3 -> "冻结";
            default -> String.valueOf(status);
        };
    }

    @Override
    public List<AssetTypeVO> listAssetTypes() {
        List<AssetType> types = assetTypeMapper.selectAll();
        return types.stream().map(t -> AssetTypeVO.builder()
                .assetTypeId(t.getAssetTypeId())
                .assetTypeName(t.getAssetTypeName())
                .build()).collect(Collectors.toList());
    }
}
