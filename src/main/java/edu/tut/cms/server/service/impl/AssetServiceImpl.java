package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.AssetAddDTO;
import edu.tut.cms.pojo.dto.AssetTypeAddDTO;
import edu.tut.cms.pojo.dto.AssetUpdateDTO;
import edu.tut.cms.pojo.entity.Asset;
import edu.tut.cms.pojo.entity.AssetType;
import edu.tut.cms.pojo.entity.Building;
import edu.tut.cms.pojo.vo.AssetTypeVO;
import edu.tut.cms.pojo.vo.AssetVO;
import edu.tut.cms.server.mapper.AssetMapper;
import edu.tut.cms.server.mapper.AssetTypeMapper;
import edu.tut.cms.server.mapper.BuildingMapper;
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
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetTypeMapper assetTypeMapper;
    private final BuildingMapper buildingMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAsset(AssetAddDTO dto) {
        Asset asset = Asset.builder()
                .assetCode(generateAssetCode(dto.getAssetType(), dto.getAssetBuilding(), dto.getAssetFloor()))
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
        if (dto.getAssetBuilding() != null && dto.getAssetFloor() != null) {
            buildLocationCode(dto.getAssetBuilding(), dto.getAssetFloor());
        }
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
                                          Long assetOwnerId, String ownerUid, Integer assetType,
                                          Integer assetStatus, Long assetBuilding) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() != null && currentUser.getUserRole() >= 4) {
            assetOwnerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        List<AssetVO> list = assetMapper.selectPage(assetOwnerId, ownerUid, assetType, assetStatus, assetBuilding);
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
        if (currentUser.getUserRole() != null && currentUser.getUserRole() >= 4) {
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
                row.createCell(8).setCellValue(formatBuilding(asset));
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

    private String formatBuilding(AssetVO asset) {
        if (asset.getBuildingCode() == null && asset.getBuildingName() == null) {
            return asset.getAssetBuilding() != null ? String.valueOf(asset.getAssetBuilding()) : "";
        }
        String code = asset.getBuildingCode() != null ? asset.getBuildingCode() : "";
        String name = asset.getBuildingName() != null ? asset.getBuildingName() : "";
        return (code + " " + name).trim();
    }

    @Override
    public List<AssetTypeVO> listAssetTypes() {
        List<AssetType> types = assetTypeMapper.selectAll();
        return types.stream().map(t -> AssetTypeVO.builder()
                .assetTypeId(t.getAssetTypeId())
                .assetTypeName(t.getAssetTypeName())
                .assetTypeShort(t.getAssetTypeShort())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAssetType(AssetTypeAddDTO dto) {
        Integer nextTypeId = assetTypeMapper.selectMaxTypeId() + 1;
        AssetType type = AssetType.builder()
                .assetTypeId(nextTypeId)
                .assetTypeName(dto.getAssetTypeName())
                .assetTypeShort(normalizeAssetTypeShort(dto.getAssetTypeShort()))
                .build();
        assetTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssetType(AssetTypeAddDTO dto) {
        AssetType type = AssetType.builder()
                .assetTypeId(dto.getAssetTypeId())
                .assetTypeName(dto.getAssetTypeName())
                .assetTypeShort(normalizeAssetTypeShort(dto.getAssetTypeShort()))
                .build();
        assetTypeMapper.updateById(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssetType(Integer id) {
        if (assetMapper.countByAssetTypeId(id) > 0) {
            throw new BusinessException("该资产类型已被资产使用，无法删除");
        }
        assetTypeMapper.deleteById(id);
    }

    private String generateAssetCode(Integer assetTypeId, Long buildingId, String floor) {
        AssetType type = assetTypeMapper.selectById(assetTypeId);
        if (type == null) {
            throw new BusinessException("资产类型不存在");
        }
        String typeShort = normalizeAssetTypeShort(type.getAssetTypeShort());
        String suffix = buildLocationCode(buildingId, floor);
        for (int i = 0; i < 20; i++) {
            String code = typeShort + suffix + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
            if (assetMapper.countByCode(code) == 0) {
                return code;
            }
        }
        throw new BusinessException("资产编号生成失败，请重试");
    }

    private String buildLocationCode(Long buildingId, String floor) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException("建筑物不存在");
        }
        Integer floorNumber;
        try {
            floorNumber = Integer.valueOf(floor);
        } catch (Exception e) {
            throw new BusinessException("楼层必须为数字");
        }
        if (floorNumber < building.getBuildingMinFloor() || floorNumber > building.getBuildingMaxFloor()) {
            throw new BusinessException("楼层不在建筑物楼层范围内");
        }
        String buildingCode = building.getBuildingCode();
        if (buildingCode == null || buildingCode.length() < 2) {
            throw new BusinessException("建筑物编号不正确");
        }
        return buildingCode.substring(buildingCode.length() - 2) + String.format("%02d", floorNumber);
    }

    private String normalizeAssetTypeShort(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z]{1,2}")) {
            throw new BusinessException("资产类型缩写必须为一至两位大写英文字母");
        }
        return normalized;
    }
}
