package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.FacilityAddDTO;
import edu.tut.cms.pojo.dto.FacilityTypeAddDTO;
import edu.tut.cms.pojo.dto.FacilityUpdateDTO;
import edu.tut.cms.pojo.entity.Facility;
import edu.tut.cms.pojo.entity.FacilityType;
import edu.tut.cms.pojo.vo.FacilityTypeVO;
import edu.tut.cms.pojo.vo.FacilityVO;
import edu.tut.cms.server.mapper.FacilityMapper;
import edu.tut.cms.server.mapper.FacilityTypeMapper;
import edu.tut.cms.server.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper facilityMapper;
    private final FacilityTypeMapper facilityTypeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFacility(FacilityAddDTO dto) {
        Facility facility = Facility.builder()
                .facilityCode(dto.getFacilityCode())
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
}
