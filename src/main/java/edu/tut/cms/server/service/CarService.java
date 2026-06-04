package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.CarAddDTO;
import edu.tut.cms.pojo.dto.CarUpdateDTO;
import edu.tut.cms.pojo.vo.CarTypeVO;
import edu.tut.cms.pojo.vo.CarVO;

import java.util.List;

public interface CarService {

    /**
     * 新增车辆
     *
     * @param dto 车辆信息
     */
    void addCar(CarAddDTO dto);

    /**
     * 更新车辆信息
     *
     * @param dto 车辆更新信息
     */
    void updateCar(CarUpdateDTO dto);

    /**
     * 删除车辆
     *
     * @param id 车辆ID
     */
    void deleteCar(Long id);

    /**
     * 分页查询车辆列表
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param ownerId   业主ID
     * @param carType   车辆类型
     * @param carStatus 车辆状态
     * @return 分页结果
     */
    PageResult<CarVO> pageCars(Integer pageNum, Integer pageSize,
                               Long ownerId, String ownerUid, String carNumber,
                               Integer carType, Integer carStatus);

    /**
     * 获取所有车辆类型
     *
     * @return 车辆类型列表
     */
    List<CarTypeVO> listCarTypes();
}
