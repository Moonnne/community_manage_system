package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.Task;
import edu.tut.cms.pojo.vo.TaskVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO task(task_code, task_title, task_info, owner_id, task_operator_id, task_status, task_type, task_create_time, task_remark) VALUES(#{taskCode}, #{taskTitle}, #{taskInfo}, #{ownerId}, #{taskOperatorId}, #{taskStatus}, #{taskType}, #{taskCreateTime}, #{taskRemark})")
    int insert(Task task);

    int updateById(Task task);

    @Delete("DELETE FROM task WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM task WHERE id = #{id}")
    Task selectById(Long id);

    @Select("SELECT COUNT(1) FROM task WHERE task_code = #{taskCode}")
    int countByCode(String taskCode);

    @Select("SELECT COUNT(1) FROM task WHERE task_type = #{taskTypeId}")
    int countByTaskTypeId(Integer taskTypeId);

    List<TaskVO> selectPage(@Param("ownerId") Long ownerId,
                            @Param("taskOperatorId") Long taskOperatorId,
                            @Param("taskStatus") Integer taskStatus,
                            @Param("taskType") Integer taskType,
                            @Param("excludeComplaint") Boolean excludeComplaint);
}
