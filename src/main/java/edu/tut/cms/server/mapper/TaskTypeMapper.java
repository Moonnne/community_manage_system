package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.TaskType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskTypeMapper {

    @Select("SELECT * FROM task_type ORDER BY task_type_id ASC")
    List<TaskType> selectAll();

    @Select("SELECT * FROM task_type WHERE task_type_id = #{taskTypeId}")
    TaskType selectById(Integer taskTypeId);

    @Insert("INSERT INTO task_type(task_type_id, task_type_name, task_type_remark) VALUES(#{taskTypeId}, #{taskTypeName}, #{taskTypeRemark})")
    int insert(TaskType type);

    @Delete("DELETE FROM task_type WHERE task_type_id = #{taskTypeId}")
    int deleteById(Integer taskTypeId);

    @Select("SELECT COALESCE(MAX(task_type_id), 0) FROM task_type")
    Integer selectMaxTypeId();
}
