package edu.tut.cms.server.controller;

import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.UserRoleVO;
import edu.tut.cms.pojo.vo.UserVO;
import edu.tut.cms.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     * @param dto 前端传入JSON
     * @return 用户数据JSON
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserVO> info() {
        CurrentUser cur = UserContext.getCurrentUser();
        return Result.success(userService.getUserInfo(cur.getId()));
    }

    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody UserPasswordDTO dto) {
        userService.changePassword(dto);
        return Result.success();
    }

    @PostMapping("/add")
    @RequireRole({0})
    public Result<Void> add(@Valid @RequestBody UserAddDTO dto) {
        userService.addUser(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    @RequireRole({0})
    public Result<Void> delete(@RequestParam Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PostMapping("/status")
    @RequireRole({0, 1})
    public Result<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        userService.updateUserStatus(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @RequirePermission(minLevel = 2)
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String uid,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam(required = false) String userPhone,
                                           @RequestParam(required = false) String userEmail,
                                           @RequestParam(required = false) Integer userRole,
                                           @RequestParam(required = false) Integer userStatus) {
        return Result.success(userService.pageUsers(pageNum, pageSize, uid, userName, userPhone, userEmail,
                userRole, userStatus));
    }

    @GetMapping("/roles")
    public Result<List<UserRoleVO>> roles() {
        return Result.success(userService.listRoles());
    }

    @PostMapping("/role/add")
    @RequireRole({0})
    public Result<Void> addRole(@Valid @RequestBody UserRoleAddDTO dto) {
        userService.addRole(dto);
        return Result.success();
    }

    @PostMapping("/role/delete")
    @RequireRole({0})
    public Result<Void> deleteRole(@RequestParam Integer id) {
        userService.deleteRole(id);
        return Result.success();
    }
}
