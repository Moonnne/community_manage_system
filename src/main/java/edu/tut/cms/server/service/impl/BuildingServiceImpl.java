package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.BuildingAddDTO;
import edu.tut.cms.pojo.dto.BuildingUpdateDTO;
import edu.tut.cms.pojo.entity.Building;
import edu.tut.cms.pojo.vo.BuildingVO;
import edu.tut.cms.server.mapper.BuildingMapper;
import edu.tut.cms.server.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBuilding(BuildingAddDTO dto) {
        validateFloors(dto.getBuildingMinFloor(), dto.getBuildingMaxFloor());
        Long nextId = buildingMapper.selectMaxId() + 1;
        Building building = Building.builder()
                .id(nextId)
                .buildingCode(String.format("B%04d", nextId))
                .buildingName(dto.getBuildingName())
                .buildingMinFloor(dto.getBuildingMinFloor())
                .buildingMaxFloor(dto.getBuildingMaxFloor())
                .buildingLocation(dto.getBuildingLocation())
                .buildingRemark(dto.getBuildingRemark())
                .build();
        buildingMapper.insert(building);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuilding(BuildingUpdateDTO dto) {
        if (dto.getBuildingMinFloor() != null && dto.getBuildingMaxFloor() != null) {
            validateFloors(dto.getBuildingMinFloor(), dto.getBuildingMaxFloor());
        }
        Building building = Building.builder()
                .id(dto.getId())
                .buildingName(dto.getBuildingName())
                .buildingMinFloor(dto.getBuildingMinFloor())
                .buildingMaxFloor(dto.getBuildingMaxFloor())
                .buildingLocation(dto.getBuildingLocation())
                .buildingRemark(dto.getBuildingRemark())
                .build();
        buildingMapper.updateById(building);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuilding(Long id) {
        if (buildingMapper.countAssets(id) > 0 || buildingMapper.countFacilities(id) > 0) {
            throw new BusinessException("该建筑物已被资产或设施使用，无法删除");
        }
        buildingMapper.deleteById(id);
    }

    @Override
    public PageResult<BuildingVO> pageBuildings(Integer pageNum,
                                                Integer pageSize,
                                                String buildingCode,
                                                String buildingName,
                                                String buildingLocation,
                                                String buildingRemark) {
        PageHelper.startPage(pageNum, pageSize);
        List<BuildingVO> list = buildingMapper.selectPage(buildingCode, buildingName, buildingLocation, buildingRemark);
        PageInfo<BuildingVO> pageInfo = new PageInfo<>(list);
        PageResult<BuildingVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    public List<BuildingVO> listBuildings() {
        return buildingMapper.selectPage(null, null, null, null);
    }

    private void validateFloors(Integer minFloor, Integer maxFloor) {
        if (minFloor == null || maxFloor == null || minFloor > maxFloor) {
            throw new BusinessException("建筑物楼层范围不正确");
        }
    }
}
