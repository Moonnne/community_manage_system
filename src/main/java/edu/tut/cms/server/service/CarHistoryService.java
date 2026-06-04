package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.CarHistoryAddDTO;
import edu.tut.cms.pojo.dto.CarHistoryBatchDeleteDTO;
import edu.tut.cms.pojo.dto.CarHistoryUpdateDTO;
import edu.tut.cms.pojo.vo.CarHistoryVO;

public interface CarHistoryService {

    /**
     * 新增车辆出入记录，同时更新车辆状态
     *
     * @param dto 出入记录信息
     */
    void addCarHistory(CarHistoryAddDTO dto);

    void updateCarHistory(CarHistoryUpdateDTO dto);

    void deleteCarHistory(Long id);

    void batchDeleteCarHistory(CarHistoryBatchDeleteDTO dto);

    /**
     * 分页查询车辆出入记录
     *
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @param carId          车辆ID
     * @param gateId         门岗ID
     * @param carHistoryType 出入类型
     * @return 分页结果
     */
    PageResult<CarHistoryVO> pageCarHistory(Integer pageNum, Integer pageSize,
                                            String carNumber, Integer gateId,
                                            Integer carHistoryType,
                                            java.time.LocalDateTime rangeStart,
                                            java.time.LocalDateTime rangeEnd);
}
