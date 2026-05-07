package edu.tut.cms.server.service;

import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.UserRoleVO;
import edu.tut.cms.pojo.vo.UserVO;

import java.util.List;

public interface UserService {

    /**
     * 用户登录
     *
     * @param dto 登录信息（用户 uid、密码）
     * @return 用户信息（含token）
     */
    UserVO login(UserLoginDTO dto);

    /**
     * 用户注册
     *
     * @param dto 注册信息
     */
    void register(UserRegisterDTO dto);

    /**
     * 获取用户详细信息
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param dto 更新信息
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * 修改密码
     *
     * @param dto 旧密码和新密码
     */
    void changePassword(UserPasswordDTO dto);

    /**
     * 管理员添加用户
     *
     * @param dto 用户信息
     */
    void addUser(UserAddDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 更新用户状态
     *
     * @param dto 用户ID和目标状态
     */
    void updateUserStatus(UserStatusDTO dto);

    /**
     * 分页查询用户列表
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param uid         用户 uid（模糊）
     * @param userName    昵称（模糊）
     * @param userPhone   手机号（模糊）
     * @param userEmail   邮箱（模糊）
     * @param userRole    角色
     * @param userStatus  状态
     * @return 分页结果
     */
    PageResult<UserVO> pageUsers(Integer pageNum, Integer pageSize,
                                 String uid, String userName, String userPhone, String userEmail,
                                 Integer userRole, Integer userStatus);

    /**
     * 获取所有角色列表
     *
     * @return 角色列表
     */
    List<UserRoleVO> listRoles();

    /**
     * 新增角色组
     *
     * @param dto 角色组信息
     */
    void addRole(UserRoleAddDTO dto);

    /**
     * 删除角色组
     *
     * @param roleId 角色组ID
     */
    void deleteRole(Integer roleId);
}
