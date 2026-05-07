package edu.tut.cms.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.utils.JwtUtils;
import edu.tut.cms.common.utils.PasswordUtils;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.entity.User;
import edu.tut.cms.pojo.entity.UserRole;
import edu.tut.cms.pojo.vo.UserRoleVO;
import edu.tut.cms.pojo.vo.UserVO;
import edu.tut.cms.server.mapper.UserMapper;
import edu.tut.cms.server.mapper.UserRoleMapper;
import edu.tut.cms.server.service.UserService;
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
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtUtils jwtUtils;

    @Override
    public UserVO login(UserLoginDTO dto) {
        User user = userMapper.selectByUid(dto.getUid());
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (!PasswordUtils.matches(dto.getUserPassword(), user.getUserPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getUserStatus() != null && user.getUserStatus() == 2) {
            throw new BusinessException("账号已被禁用");
        }

        UserRole role = userRoleMapper.selectById(user.getUserRole());
        CurrentUser currentUser = CurrentUser.builder()
                .id(user.getId())
                .uid(user.getUid())
                .userName(user.getUserName())
                .userRole(user.getUserRole())
                .userStatus(user.getUserStatus())
                .build();
        String token = jwtUtils.generateToken(currentUser);

        return UserVO.builder()
                .id(user.getId())
                .uid(user.getUid())
                .userName(user.getUserName())
                .userBirthday(user.getUserBirthday())
                .userGender(user.getUserGender())
                .userPhone(user.getUserPhone())
                .userEmail(user.getUserEmail())
                .userRole(user.getUserRole())
                .roleName(role != null ? role.getRoleName() : null)
                .userStatus(user.getUserStatus())
                .userCreateTime(user.getUserCreateTime())
                .userAvata(user.getUserAvata())
                .token(token)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        User existingUid = userMapper.selectByUid(dto.getUid());
        if (existingUid != null) {
            throw new BusinessException("改账号已存在");
        }

        User user = User.builder()
                .uid(dto.getUid())
                .userName(dto.getUserName())
                .userPassword(PasswordUtils.encode(dto.getUserPassword()))
                .userRole(4)
                .userStatus(0)
                .userCreateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserRole role = userRoleMapper.selectById(user.getUserRole());
        return UserVO.builder()
                .id(user.getId())
                .uid(user.getUid())
                .userName(user.getUserName())
                .userBirthday(user.getUserBirthday())
                .userGender(user.getUserGender())
                .userPhone(user.getUserPhone())
                .userEmail(user.getUserEmail())
                .userRole(user.getUserRole())
                .roleName(role != null ? role.getRoleName() : null)
                .userStatus(user.getUserStatus())
                .userCreateTime(user.getUserCreateTime())
                .userAvata(user.getUserAvata())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        if (currentUser.getUserRole() >= 4 && !currentUser.getId().equals(dto.getId())) {
            throw new BusinessException("无权修改其他用户信息");
        }

        User user = User.builder()
                .id(dto.getId())
                .userName(dto.getUserName())
                .userBirthday(dto.getUserBirthday())
                .userGender(dto.getUserGender())
                .userPhone(dto.getUserPhone())
                .userEmail(dto.getUserEmail())
                .userAvata(dto.getUserAvata())
                .build();
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserPasswordDTO dto) {
        CurrentUser currentUser = UserContext.getCurrentUser();
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtils.matches(dto.getOldPassword(), user.getUserPassword())) {
            throw new BusinessException("原密码错误");
        }

        User update = User.builder()
                .id(user.getId())
                .userPassword(PasswordUtils.encode(dto.getNewPassword()))
                .build();
        userMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddDTO dto) {
        if (userMapper.selectByUid(dto.getUid()) != null) {
            throw new BusinessException("用户标识已存在");
        }
        if (userMapper.selectByUserName(dto.getUserName()) != null) {
            throw new BusinessException("昵称已存在");
        }

        User user = User.builder()
                .uid(dto.getUid())
                .userName(dto.getUserName())
                .userPassword(PasswordUtils.encode(dto.getUserPassword()))
                .userBirthday(dto.getUserBirthday())
                .userGender(dto.getUserGender())
                .userPhone(dto.getUserPhone())
                .userEmail(dto.getUserEmail())
                .userRole(dto.getUserRole())
                .userStatus(dto.getUserStatus())
                .userCreateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(UserStatusDTO dto) {
        userMapper.updateStatus(dto.getId(), dto.getUserStatus());
    }

    @Override
    public PageResult<UserVO> pageUsers(Integer pageNum, Integer pageSize,
                                        String uid, String userName, String userPhone, String userEmail,
                                        Integer userRole, Integer userStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectPage(uid, userName, userPhone, userEmail, userRole, userStatus);
        PageInfo<User> pageInfo = new PageInfo<>(list);

        List<UserVO> voList = list.stream().map(user -> {
            UserRole role = userRoleMapper.selectById(user.getUserRole());
            return UserVO.builder()
                    .id(user.getId())
                    .uid(user.getUid())
                    .userName(user.getUserName())
                    .userBirthday(user.getUserBirthday())
                    .userGender(user.getUserGender())
                    .userPhone(user.getUserPhone())
                    .userEmail(user.getUserEmail())
                    .userRole(user.getUserRole())
                    .roleName(role != null ? role.getRoleName() : null)
                    .userStatus(user.getUserStatus())
                    .userCreateTime(user.getUserCreateTime())
                    .userAvata(user.getUserAvata())
                    .build();
        }).collect(Collectors.toList());

        PageResult<UserVO> result = new PageResult<>();
        result.setTotal(pageInfo.getTotal());
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setPages(pageInfo.getPages());
        result.setRecords(voList);
        return result;
    }

    @Override
    public List<UserRoleVO> listRoles() {
        List<UserRole> roles = userRoleMapper.selectAll();
        return roles.stream().map(r -> UserRoleVO.builder()
                .roleId(r.getRoleId())
                .roleName(r.getRoleName())
                .rolePromission(r.getRolePromission())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(UserRoleAddDTO dto) {
        Integer nextRoleId = userRoleMapper.selectMaxRoleId() + 1;
        UserRole role = UserRole.builder()
                .roleId(nextRoleId)
                .roleName(dto.getRoleName())
                .rolePromission(dto.getRolePromission())
                .build();
        userRoleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer roleId) {
        if (roleId == null) {
            throw new BusinessException("角色组不存在");
        }
        if (userMapper.countByRoleId(roleId) > 0) {
            throw new BusinessException("该角色组下仍有关联用户，无法删除");
        }
        userRoleMapper.deleteById(roleId);
    }
}
