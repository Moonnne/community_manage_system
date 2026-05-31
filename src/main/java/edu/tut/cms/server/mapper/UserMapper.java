package edu.tut.cms.server.mapper;

import edu.tut.cms.pojo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    User selectByUserName(String userName);

    @Select("SELECT * FROM user WHERE uid = #{uid}")
    User selectByUid(String uid);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Insert("INSERT INTO user(uid, user_name, user_birthday, user_gender, user_phone, user_email, user_password, user_role, user_status, user_create_time, user_avata, user_initialized, uid_locked) VALUES(#{uid}, #{userName}, #{userBirthday}, #{userGender}, #{userPhone}, #{userEmail}, #{userPassword}, #{userRole}, #{userStatus}, #{userCreateTime}, #{userAvata}, #{userInitialized}, #{uidLocked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT COUNT(1) FROM user WHERE uid = #{uid} AND id <> #{id}")
    int countByUidExcludeId(@Param("uid") String uid, @Param("id") Long id);

    int updateById(User user);

    @Update("UPDATE user SET user_status = #{userStatus} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("userStatus") Integer userStatus);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(1) FROM user WHERE user_role = #{roleId}")
    int countByRoleId(Integer roleId);

    List<User> selectPage(@Param("uid") String uid,
                          @Param("userName") String userName,
                          @Param("userPhone") String userPhone,
                          @Param("userEmail") String userEmail,
                          @Param("userRole") Integer userRole,
                          @Param("userStatus") Integer userStatus);
}
