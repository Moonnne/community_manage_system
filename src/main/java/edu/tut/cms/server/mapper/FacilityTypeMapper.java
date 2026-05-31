package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.FacilityType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FacilityTypeMapper {

    @Select("SELECT * FROM facility_type ORDER BY id ASC")
    List<FacilityType> selectAll();

    @Select("SELECT * FROM facility_type WHERE facility_type_id = #{facilityTypeId}")
    FacilityType selectById(Integer facilityTypeId);

    @Insert("INSERT INTO facility_type(facility_type_id, facility_type_name, facility_type_remark) VALUES(#{facilityTypeId}, #{facilityTypeName}, #{facilityTypeRemark})")
    int insert(FacilityType type);

    @Delete("DELETE FROM facility_type WHERE facility_type_id = #{facilityTypeId}")
    int deleteById(Integer facilityTypeId);
}
