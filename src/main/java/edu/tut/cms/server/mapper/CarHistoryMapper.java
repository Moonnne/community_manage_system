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

    List<CarHistoryVO> selectPage(@Param("carId") Long carId,
                                  @Param("gateId") Integer gateId,
                                  @Param("carHistoryType") Integer carHistoryType);
}
