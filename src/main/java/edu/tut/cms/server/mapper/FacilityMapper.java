package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Facility;
import edu.tut.cms.pojo.vo.FacilityVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FacilityMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO facility(facility_code, facility_name, facility_chief_id, facility_type, facility_status, facility_check_time, facility_expiration_date, facility_building, facility_floor, facility_position, facility_owner, facility_remark) VALUES(#{facilityCode}, #{facilityName}, #{facilityChiefId}, #{facilityType}, #{facilityStatus}, #{facilityCheckTime}, #{facilityExpirationDate}, #{facilityBuilding}, #{facilityFloor}, #{facilityPosition}, #{facilityOwner}, #{facilityRemark})")
    int insert(Facility facility);

    int updateById(Facility facility);

    @Delete("DELETE FROM facility WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM facility WHERE id = #{id}")
    Facility selectById(Long id);

    @Select("SELECT COUNT(1) FROM facility WHERE facility_code = #{facilityCode}")
    int countByCode(String facilityCode);

    List<FacilityVO> selectPage(@Param("facilityType") Integer facilityType,
                                @Param("facilityStatus") Integer facilityStatus,
                                @Param("facilityCode") String facilityCode,
                                @Param("facilityName") String facilityName,
                                @Param("facilityBuilding") Long facilityBuilding);

    List<FacilityVO> selectNearExpiration(@Param("months") int months);
}
