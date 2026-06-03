package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Building;
import edu.tut.cms.pojo.vo.BuildingVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingMapper {

    @Insert("INSERT INTO building(id, building_code, building_name, building_min_floor, building_max_floor, building_location, building_remark) VALUES(#{id}, #{buildingCode}, #{buildingName}, #{buildingMinFloor}, #{buildingMaxFloor}, #{buildingLocation}, #{buildingRemark})")
    int insert(Building building);

    int updateById(Building building);

    @Delete("DELETE FROM building WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM building WHERE id = #{id}")
    Building selectById(Long id);

    @Select("SELECT COALESCE(MAX(id), 0) FROM building")
    Long selectMaxId();

    @Select("SELECT COUNT(1) FROM asset WHERE asset_building = #{id}")
    int countAssets(Long id);

    @Select("SELECT COUNT(1) FROM facility WHERE facility_building = #{id}")
    int countFacilities(Long id);

    List<BuildingVO> selectPage(@Param("buildingCode") String buildingCode,
                                @Param("buildingName") String buildingName,
                                @Param("buildingLocation") String buildingLocation,
                                @Param("buildingRemark") String buildingRemark);
}
