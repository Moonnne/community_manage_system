package edu.tut.cms.server.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import edu.tut.cms.common.aop.RequirePermission;
import edu.tut.cms.common.aop.RequireRole;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.UserRoleVO;
import edu.tut.cms.pojo.vo.UserVO;
import edu.tut.cms.server.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final String CAPTCHA_SESSION_KEY = "LOGIN_REGISTER_CAPTCHA";

    private final UserService userService;

    @GetMapping("/captcha")
    public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        session.setAttribute(CAPTCHA_SESSION_KEY, captcha.getCode().toLowerCase(Locale.ROOT));
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        captcha.write(response.getOutputStream());
    }

    /**
     * 用户登录
     * @param dto 前端传入JSON
     * @return 用户数据JSON
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO dto, HttpSession session) {
        validateCaptcha(dto.getCaptchaCode(), session);
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto, HttpSession session) {
        validateCaptcha(dto.getCaptchaCode(), session);
        userService.register(dto);
        return Result.success();
    }

    private void validateCaptcha(String captchaCode, HttpSession session) {
        Object expected = session.getAttribute(CAPTCHA_SESSION_KEY);
        session.removeAttribute(CAPTCHA_SESSION_KEY);
        if (expected == null || captchaCode == null
                || !expected.toString().equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("验证码错误或已过期");
        }
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
