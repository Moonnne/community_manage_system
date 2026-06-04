package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.CarHistory;
import edu.tut.cms.pojo.vo.CarHistoryVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CarHistoryMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO car_history(car_history_code, car_history_operator_id, car_id, gate_id, car_history_type, car_history_time, car_history_remark) VALUES(#{carHistoryCode}, #{carHistoryOperatorId}, #{carId}, #{gateId}, #{carHistoryType}, #{carHistoryTime}, #{carHistoryRemark})")
    int insert(CarHistory history);

    @Update("UPDATE car_history SET car_id = #{carId}, gate_id = #{gateId}, car_history_type = #{carHistoryType}, car_history_remark = #{carHistoryRemark} WHERE id = #{id}")
    int updateById(CarHistory history);

    @Delete("DELETE FROM car_history WHERE id = #{id}")
    int deleteById(Long id);

    List<CarHistoryVO> selectPage(@Param("carNumber") String carNumber,
                                  @Param("gateId") Integer gateId,
                                  @Param("carHistoryType") Integer carHistoryType,
                                  @Param("rangeStart") java.time.LocalDateTime rangeStart,
                                  @Param("rangeEnd") java.time.LocalDateTime rangeEnd);
}
