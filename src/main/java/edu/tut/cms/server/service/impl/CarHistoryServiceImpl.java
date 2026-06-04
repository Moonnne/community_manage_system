package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.CarHistoryAddDTO;
import edu.tut.cms.pojo.dto.CarHistoryBatchDeleteDTO;
import edu.tut.cms.pojo.dto.CarHistoryUpdateDTO;
import edu.tut.cms.pojo.entity.Car;
import edu.tut.cms.pojo.entity.CarHistory;
import edu.tut.cms.pojo.vo.CarHistoryVO;
import edu.tut.cms.server.mapper.CarHistoryMapper;
import edu.tut.cms.server.mapper.CarMapper;
import edu.tut.cms.server.service.CarHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarHistoryServiceImpl implements CarHistoryService {

    private final CarHistoryMapper carHistoryMapper;
    private final CarMapper carMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCarHistory(CarHistoryAddDTO dto) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        String code = "CH" + System.currentTimeMillis();

        CarHistory history = CarHistory.builder()
                .carHistoryCode(code)
                .carHistoryOperatorId(currentUser.getId())
                .carId(dto.getCarId())
                .gateId(dto.getGateId())
                .carHistoryType(dto.getCarHistoryType())
                .carHistoryTime(LocalDateTime.now())
                .carHistoryRemark(dto.getCarHistoryRemark())
                .build();
        carHistoryMapper.insert(history);

        Integer carStatus;
        if (dto.getCarHistoryType() == 0 || dto.getCarHistoryType() == 2) {
            carStatus = 0;
        } else {
            carStatus = 1;
        }
        Car car = Car.builder()
                .id(dto.getCarId())
                .carStatus(carStatus)
                .build();
        carMapper.updateById(car);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCarHistory(CarHistoryUpdateDTO dto) {
        CarHistory history = CarHistory.builder()
                .id(dto.getId())
                .carId(dto.getCarId())
                .gateId(dto.getGateId())
                .carHistoryType(dto.getCarHistoryType())
                .carHistoryRemark(dto.getCarHistoryRemark())
                .build();
        carHistoryMapper.updateById(history);

        Car car = Car.builder()
                .id(dto.getCarId())
                .carStatus(dto.getCarHistoryType() == 0 ? 0 : 1)
                .build();
        carMapper.updateById(car);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarHistory(Long id) {
        carHistoryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCarHistory(CarHistoryBatchDeleteDTO dto) {
        dto.getIds().forEach(carHistoryMapper::deleteById);
    }

    @Override
    public PageResult<CarHistoryVO> pageCarHistory(Integer pageNum, Integer pageSize,
                                                   String carNumber, Integer gateId,
                                                   Integer carHistoryType,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd) {
        PageHelper.startPage(pageNum, pageSize);
        List<CarHistoryVO> list = carHistoryMapper.selectPage(carNumber, gateId, carHistoryType, rangeStart, rangeEnd);
        PageInfo<CarHistoryVO> pageInfo = new PageInfo<>(list);

        PageResult<CarHistoryVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }
}
