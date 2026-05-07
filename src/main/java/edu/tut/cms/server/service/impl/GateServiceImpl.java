package edu.tut.cms.server.service.impl;

import edu.tut.cms.pojo.dto.GateAddDTO;
import edu.tut.cms.pojo.dto.GateUpdateDTO;
import edu.tut.cms.pojo.entity.Gate;
import edu.tut.cms.pojo.vo.GateVO;
import edu.tut.cms.server.mapper.GateMapper;
import edu.tut.cms.server.service.GateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GateServiceImpl implements GateService {

    private final GateMapper gateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGate(GateAddDTO dto) {
        Gate gate = Gate.builder()
                .gateCode(dto.getGateCode())
                .gateName(dto.getGateName())
                .gateChiefId(dto.getGateChiefId())
                .gateRemark(dto.getGateRemark())
                .build();
        gateMapper.insert(gate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGate(GateUpdateDTO dto) {
        Gate gate = Gate.builder()
                .id(dto.getId())
                .gateName(dto.getGateName())
                .gateChiefId(dto.getGateChiefId())
                .gateRemark(dto.getGateRemark())
                .build();
        gateMapper.updateById(gate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGate(Integer id) {
        gateMapper.deleteById(id);
    }

    @Override
    public List<GateVO> listGates() {
        return gateMapper.selectAll();
    }
}
