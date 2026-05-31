package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Select("SELECT * FROM user_role WHERE role_id = #{roleId}")
    UserRole selectById(Integer roleId);

    @Select("SELECT * FROM user_role ORDER BY role_id ASC")
    List<UserRole> selectAll();

    @Insert("INSERT INTO user_role(role_id, role_name, role_promission) VALUES(#{roleId}, #{roleName}, #{rolePromission})")
    int insert(UserRole userRole);

    @Delete("DELETE FROM user_role WHERE role_id = #{roleId}")
    int deleteById(Integer roleId);

    @Select("SELECT COALESCE(MAX(role_id), -1) FROM user_role")
    Integer selectMaxRoleId();
}
