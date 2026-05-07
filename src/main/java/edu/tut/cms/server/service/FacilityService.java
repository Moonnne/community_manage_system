package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.FacilityAddDTO;
import edu.tut.cms.pojo.dto.FacilityTypeAddDTO;
import edu.tut.cms.pojo.dto.FacilityUpdateDTO;
import edu.tut.cms.pojo.vo.FacilityTypeVO;
import edu.tut.cms.pojo.vo.FacilityVO;

import java.util.List;

public interface FacilityService {

    /**
     * 新增设施
     *
     * @param dto 设施信息
     */
    void addFacility(FacilityAddDTO dto);

    /**
     * 更新设施信息
     *
     * @param dto 设施更新信息
     */
    void updateFacility(FacilityUpdateDTO dto);

    /**
     * 删除设施
     *
     * @param id 设施ID
     */
    void deleteFacility(Long id);

    /**
     * 分页查询设施列表
     *
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @param facilityType   设施类型
     * @param facilityStatus 设施状态
     * @param facilityName   设施名称（模糊）
     * @return 分页结果
     */
    PageResult<FacilityVO> pageFacilities(Integer pageNum, Integer pageSize,
                                          Integer facilityType, Integer facilityStatus,
                                          String facilityName);

    /**
     * 获取即将到期的设施列表
     *
     * @param months 到期月数范围
     * @return 设施列表
     */
    List<FacilityVO> getNearExpirationFacilities(int months);

    /**
     * 获取所有设施类型
     *
     * @return 设施类型列表
     */
    List<FacilityTypeVO> listFacilityTypes();

    /**
     * 新增设施类型
     *
     * @param dto 设施类型信息
     */
    void addFacilityType(FacilityTypeAddDTO dto);

    /**
     * 删除设施类型
     *
     * @param id 类型ID
     */
    void deleteFacilityType(Integer id);
}
