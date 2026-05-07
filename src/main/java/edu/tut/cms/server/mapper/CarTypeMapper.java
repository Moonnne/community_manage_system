package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.CarType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CarTypeMapper {

    @Select("SELECT * FROM car_type")
    List<CarType> selectAll();

    @Select("SELECT * FROM car_type WHERE car_type_id = #{carTypeId}")
    CarType selectById(Integer carTypeId);
}
