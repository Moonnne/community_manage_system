package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Car;
import edu.tut.cms.pojo.vo.CarVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CarMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO car(car_code, owner_id, asset_id, visitor_id, car_number, car_type, car_status) VALUES(#{carCode}, #{ownerId}, #{assetId}, #{visitorId}, #{carNumber}, #{carType}, #{carStatus})")
    int insert(Car car);

    int updateById(Car car);

    @Delete("DELETE FROM car WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM car WHERE id = #{id}")
    Car selectById(Long id);

    List<CarVO> selectPage(@Param("ownerId") Long ownerId,
                           @Param("carType") Integer carType,
                           @Param("carStatus") Integer carStatus);
}
