package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.TaskAddDTO;
import edu.tut.cms.pojo.dto.TaskTypeAddDTO;
import edu.tut.cms.pojo.dto.TaskUpdateDTO;
import edu.tut.cms.pojo.vo.TaskTypeVO;
import edu.tut.cms.pojo.vo.TaskVO;

import java.util.List;

public interface TaskService {

    /**
     * 新增工单
     *
     * @param dto 工单信息
     */
    void addTask(TaskAddDTO dto);

    /**
     * 更新工单信息，自动设置更新时间
     *
     * @param dto 工单更新信息
     */
    void updateTask(TaskUpdateDTO dto);

    /**
     * 删除工单
     *
     * @param id 工单ID
     */
    void deleteTask(Long id);

    /**
     * 分页查询工单列表
     *
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @param ownerId        发起人ID
     * @param taskOperatorId 处理人ID
     * @param taskStatus     工单状态
     * @param taskType       工单类型
     * @return 分页结果
     */
    PageResult<TaskVO> pageTasks(Integer pageNum, Integer pageSize,
                                 Long ownerId, Long taskOperatorId,
                                 Integer taskStatus, Integer taskType);

    /**
     * 处理工单（分配处理人、变更状态）
     *
     * @param id         工单ID
     * @param status     目标状态
     * @param operatorId 处理人ID
     * @param remark     备注
     */
    void processTask(Long id, Integer status, Long operatorId, String remark);

    /**
     * 获取所有工单类型
     *
     * @return 工单类型列表
     */
    List<TaskTypeVO> listTaskTypes();

    /**
     * 新增工单类型
     *
     * @param dto 工单类型信息
     */
    void addTaskType(TaskTypeAddDTO dto);

    /**
     * 删除工单类型
     *
     * @param id 工单类型ID
     */
    void deleteTaskType(Integer id);
}
