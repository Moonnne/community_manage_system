package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.pojo.dto.FacilityAddDTO;
import edu.tut.cms.pojo.dto.FacilityTypeAddDTO;
import edu.tut.cms.pojo.dto.FacilityUpdateDTO;
import edu.tut.cms.pojo.entity.Building;
import edu.tut.cms.pojo.entity.Facility;
import edu.tut.cms.pojo.entity.FacilityType;
import edu.tut.cms.pojo.vo.FacilityTypeVO;
import edu.tut.cms.pojo.vo.FacilityVO;
import edu.tut.cms.server.mapper.BuildingMapper;
import edu.tut.cms.server.mapper.FacilityMapper;
import edu.tut.cms.server.mapper.FacilityTypeMapper;
import edu.tut.cms.server.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper facilityMapper;
    private final FacilityTypeMapper facilityTypeMapper;
    private final BuildingMapper buildingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFacility(FacilityAddDTO dto) {
        Facility facility = Facility.builder()
                .facilityCode(generateFacilityCode(dto.getFacilityBuilding(), dto.getFacilityFloor()))
                .facilityName(dto.getFacilityName())
                .facilityChiefId(dto.getFacilityChiefId())
                .facilityType(dto.getFacilityType())
                .facilityStatus(dto.getFacilityStatus())
                .facilityCheckTime(dto.getFacilityCheckTime())
                .facilityExpirationDate(dto.getFacilityExpirationDate())
                .facilityBuilding(dto.getFacilityBuilding())
                .facilityFloor(dto.getFacilityFloor())
                .facilityPosition(dto.getFacilityPosition())
                .facilityOwner(dto.getFacilityOwner())
                .facilityRemark(dto.getFacilityRemark())
                .build();
        facilityMapper.insert(facility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFacility(FacilityUpdateDTO dto) {
        if (dto.getFacilityBuilding() != null && dto.getFacilityFloor() != null) {
            buildLocationCode(dto.getFacilityBuilding(), dto.getFacilityFloor());
        }
        Facility facility = Facility.builder()
                .id(dto.getId())
                .facilityName(dto.getFacilityName())
                .facilityChiefId(dto.getFacilityChiefId())
                .facilityType(dto.getFacilityType())
                .facilityStatus(dto.getFacilityStatus())
                .facilityCheckTime(dto.getFacilityCheckTime())
                .facilityExpirationDate(dto.getFacilityExpirationDate())
                .facilityBuilding(dto.getFacilityBuilding())
                .facilityFloor(dto.getFacilityFloor())
                .facilityPosition(dto.getFacilityPosition())
                .facilityOwner(dto.getFacilityOwner())
                .facilityRemark(dto.getFacilityRemark())
                .build();
        facilityMapper.updateById(facility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFacility(Long id) {
        facilityMapper.deleteById(id);
    }

    @Override
    public PageResult<FacilityVO> pageFacilities(Integer pageNum, Integer pageSize,
                                                 Integer facilityType, Integer facilityStatus,
                                                 String facilityName) {
        PageHelper.startPage(pageNum, pageSize);
        List<FacilityVO> list = facilityMapper.selectPage(facilityType, facilityStatus, facilityName);
        PageInfo<FacilityVO> pageInfo = new PageInfo<>(list);

        PageResult<FacilityVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    public List<FacilityVO> getNearExpirationFacilities(int months) {
        return facilityMapper.selectNearExpiration(months);
    }

    @Override
    public List<FacilityTypeVO> listFacilityTypes() {
        List<FacilityType> types = facilityTypeMapper.selectAll();
        return types.stream().map(t -> FacilityTypeVO.builder()
                .facilityTypeId(t.getFacilityTypeId())
                .facilityTypeName(t.getFacilityTypeName())
                .facilityTypeRemark(t.getFacilityTypeRemark())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFacilityType(FacilityTypeAddDTO dto) {
        FacilityType type = FacilityType.builder()
                .facilityTypeId(dto.getFacilityTypeId())
                .facilityTypeName(dto.getFacilityTypeName())
                .facilityTypeRemark(dto.getFacilityTypeRemark())
                .build();
        facilityTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFacilityType(Integer id) {
        facilityTypeMapper.deleteById(id);
    }

    private String generateFacilityCode(Long buildingId, String floor) {
        String suffix = buildLocationCode(buildingId, floor);
        for (int i = 0; i < 20; i++) {
            String code = "F" + suffix + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
            if (facilityMapper.countByCode(code) == 0) {
                return code;
            }
        }
        throw new BusinessException("设施编号生成失败，请重试");
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
}
