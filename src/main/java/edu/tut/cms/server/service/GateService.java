package edu.tut.cms.server.service;

import edu.tut.cms.pojo.dto.GateAddDTO;
import edu.tut.cms.pojo.dto.GateUpdateDTO;
import edu.tut.cms.pojo.vo.GateVO;

import java.util.List;

public interface GateService {

    /**
     * 新增门岗
     *
     * @param dto 门岗信息
     */
    void addGate(GateAddDTO dto);

    /**
     * 更新门岗信息
     *
     * @param dto 门岗更新信息
     */
    void updateGate(GateUpdateDTO dto);

    /**
     * 删除门岗
     *
     * @param id 门岗ID
     */
    void deleteGate(Integer id);

    /**
     * 获取所有门岗列表（含负责人姓名）
     *
     * @return 门岗列表
     */
    List<GateVO> listGates();
}
