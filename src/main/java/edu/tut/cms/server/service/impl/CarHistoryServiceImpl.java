package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.CarHistoryAddDTO;
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
    public PageResult<CarHistoryVO> pageCarHistory(Integer pageNum, Integer pageSize,
                                                   Long carId, Integer gateId,
                                                   Integer carHistoryType) {
        PageHelper.startPage(pageNum, pageSize);
        List<CarHistoryVO> list = carHistoryMapper.selectPage(carId, gateId, carHistoryType);
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
