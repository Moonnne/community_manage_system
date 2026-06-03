package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.CarAddDTO;
import edu.tut.cms.pojo.dto.CarUpdateDTO;
import edu.tut.cms.pojo.entity.Car;
import edu.tut.cms.pojo.entity.CarType;
import edu.tut.cms.pojo.vo.CarTypeVO;
import edu.tut.cms.pojo.vo.CarVO;
import edu.tut.cms.server.mapper.CarMapper;
import edu.tut.cms.server.mapper.CarTypeMapper;
import edu.tut.cms.server.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarMapper carMapper;
    private final CarTypeMapper carTypeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCar(CarAddDTO dto) {
        Car car = Car.builder()
                .carCode(generateCarCode())
                .ownerId(dto.getOwnerId())
                .assetId(dto.getAssetId())
                .visitorId(dto.getVisitorId())
                .carNumber(dto.getCarNumber())
                .carType(dto.getCarType())
                .carStatus(dto.getCarStatus())
                .build();
        carMapper.insert(car);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCar(CarUpdateDTO dto) {
        Car car = Car.builder()
                .id(dto.getId())
                .ownerId(dto.getOwnerId())
                .assetId(dto.getAssetId())
                .visitorId(dto.getVisitorId())
                .carNumber(dto.getCarNumber())
                .carType(dto.getCarType())
                .carStatus(dto.getCarStatus())
                .build();
        carMapper.updateById(car);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCar(Long id) {
        carMapper.deleteById(id);
    }

    @Override
    public PageResult<CarVO> pageCars(Integer pageNum, Integer pageSize,
                                      Long ownerId, Integer carType, Integer carStatus) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        List<CarVO> list = carMapper.selectPage(ownerId, carType, carStatus);
        PageInfo<CarVO> pageInfo = new PageInfo<>(list);

        PageResult<CarVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    public List<CarTypeVO> listCarTypes() {
        List<CarType> types = carTypeMapper.selectAll();
        return types.stream().map(t -> CarTypeVO.builder()
                .carTypeId(t.getCarTypeId())
                .carTypeName(t.getCarTypeName())
                .carTypeRemark(t.getCarTypeRemark())
                .build()).collect(Collectors.toList());
    }

    private String generateCarCode() {
        String prefix = "C" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSequence = carMapper.selectMaxSequenceByPrefix(prefix) + 1;
        return prefix + String.format("%04d", nextSequence);
    }
}
