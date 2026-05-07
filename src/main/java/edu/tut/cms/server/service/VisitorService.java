package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.VisitorAddDTO;
import edu.tut.cms.pojo.dto.VisitorTypeAddDTO;
import edu.tut.cms.pojo.dto.VisitorUpdateDTO;
import edu.tut.cms.pojo.vo.VisitorTypeVO;
import edu.tut.cms.pojo.vo.VisitorVO;

import java.util.List;

public interface VisitorService {

    /**
     * 新增访客登记
     *
     * @param dto 访客信息
     */
    void addVisitor(VisitorAddDTO dto);

    /**
     * 更新访客状态，若为已完成则设置完成时间
     *
     * @param dto 访客更新信息
     */
    void updateVisitor(VisitorUpdateDTO dto);

    /**
     * 删除访客记录
     *
     * @param id 访客ID
     */
    void deleteVisitor(Long id);

    /**
     * 分页查询访客列表
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param ownerId       业主ID
     * @param visitorType   访客类型
     * @param visitorStatus 访客状态
     * @param visitorName   访客姓名（模糊）
     * @return 分页结果
     */
    PageResult<VisitorVO> pageVisitors(Integer pageNum, Integer pageSize,
                                       Long ownerId, Integer visitorType,
                                       String visitorStatus, String visitorName);

    /**
     * 获取所有访客类型
     *
     * @return 访客类型列表
     */
    List<VisitorTypeVO> listVisitorTypes();

    /**
     * 新增访客类型
     *
     * @param dto 访客类型信息
     */
    void addVisitorType(VisitorTypeAddDTO dto);

    /**
     * 删除访客类型
     *
     * @param id 类型ID
     */
    void deleteVisitorType(Integer id);
}
