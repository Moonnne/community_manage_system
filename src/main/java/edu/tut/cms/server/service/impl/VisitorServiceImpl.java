package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.VisitorAddDTO;
import edu.tut.cms.pojo.dto.VisitorTypeAddDTO;
import edu.tut.cms.pojo.dto.VisitorUpdateDTO;
import edu.tut.cms.pojo.entity.Visitor;
import edu.tut.cms.pojo.entity.VisitorType;
import edu.tut.cms.pojo.vo.VisitorTypeVO;
import edu.tut.cms.pojo.vo.VisitorVO;
import edu.tut.cms.server.mapper.VisitorMapper;
import edu.tut.cms.server.mapper.VisitorTypeMapper;
import edu.tut.cms.server.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;
    private final VisitorTypeMapper visitorTypeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVisitor(VisitorAddDTO dto) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        String visitorCode = "V" + System.currentTimeMillis();

        Visitor visitor = Visitor.builder()
                .visitorCode(visitorCode)
                .assetId(dto.getAssetId())
                .ownerId(dto.getOwnerId())
                .visitorCreater(currentUser.getId())
                .visitorName(dto.getVisitorName())
                .visitorPhone(dto.getVisitorPhone())
                .visitorType(dto.getVisitorType())
                .visitorStatus("1")
                .visitorCreateTime(LocalDateTime.now())
                .visitorRemark(dto.getVisitorRemark())
                .build();
        visitorMapper.insert(visitor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVisitor(VisitorUpdateDTO dto) {
        Visitor visitor = Visitor.builder()
                .id(dto.getId())
                .visitorStatus(dto.getVisitorStatus())
                .visitorRemark(dto.getVisitorRemark())
                .build();
        if ("0".equals(dto.getVisitorStatus())) {
            visitor.setVisitorFinishTime(LocalDateTime.now());
        }
        visitorMapper.updateById(visitor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVisitor(Long id) {
        visitorMapper.deleteById(id);
    }

    @Override
    public PageResult<VisitorVO> pageVisitors(Integer pageNum, Integer pageSize,
                                              Long ownerId, Integer visitorType,
                                              String visitorStatus, String visitorName) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4) {
            ownerId = currentUser.getId();
        }

        PageHelper.startPage(pageNum, pageSize);
        List<VisitorVO> list = visitorMapper.selectPage(ownerId, visitorType, visitorStatus, visitorName);
        PageInfo<VisitorVO> pageInfo = new PageInfo<>(list);

        PageResult<VisitorVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(pageInfo.getList());
        return result;
    }

    @Override
    public List<VisitorTypeVO> listVisitorTypes() {
        List<VisitorType> types = visitorTypeMapper.selectAll();
        return types.stream().map(t -> VisitorTypeVO.builder()
                .visitorTypeId(t.getVisitorTypeId())
                .visitorTypeName(t.getVisitorTypeName())
                .visitorTypeRemark(t.getVisitorTypeRemark())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVisitorType(VisitorTypeAddDTO dto) {
        VisitorType type = VisitorType.builder()
                .visitorTypeId(dto.getVisitorTypeId())
                .visitorTypeName(dto.getVisitorTypeName())
                .visitorTypeRemark(dto.getVisitorTypeRemark())
                .build();
        visitorTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVisitorType(Integer id) {
        visitorTypeMapper.deleteById(id);
    }
}
