package edu.tut.cms.server.controller;

import com.alibaba.fastjson2.JSON;
import edu.tut.cms.TestAopConfig;
import edu.tut.cms.common.aop.AuthAspect;
import edu.tut.cms.common.config.PageHelperConfig;
import edu.tut.cms.common.config.SecurityConfig;
import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.exception.BusinessException;
import edu.tut.cms.common.exception.GlobalExceptionHandler;
import edu.tut.cms.common.filter.JwtAuthenticationFilter;
import edu.tut.cms.common.result.PageResult;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.common.utils.JwtUtils;
import edu.tut.cms.pojo.dto.*;
import edu.tut.cms.pojo.vo.UserRoleVO;
import edu.tut.cms.pojo.vo.UserVO;
import edu.tut.cms.server.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtUtils.class,
        AuthAspect.class,
        GlobalExceptionHandler.class,
        PageHelperConfig.class,
        TestAopConfig.class
})
@TestPropertySource(properties = {
        "jwt.secret=testJwtSecretKeyMustBeAtLeast256BitsLongXxxxxxxxx",
        "jwt.expiration=86400000"
})
class UserControllerWebMvcTest {

    private static final String CAPTCHA_SESSION_KEY = "LOGIN_REGISTER_CAPTCHA";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    private String bearerForRole(int roleId) {
        CurrentUser u = CurrentUser.builder()
                .id(1L)
                .uid("u-test-1")
                .userName("nick")
                .userRole(roleId)
                .build();
        return "Bearer " + jwtUtils.generateToken(u);
    }

    @Test
    void list_withoutToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void list_withInvalidToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void list_withOwnerToken_returns403Json() throws Exception {
        when(userService.pageUsers(anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageResult<>());
        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", bearerForRole(4)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void list_withManagerToken_returns200() throws Exception {
        PageResult<UserVO> page = new PageResult<>();
        page.setTotal(0L);
        page.setPageNum(1);
        page.setPageSize(10);
        page.setPages(0);
        page.setRecords(List.of());
        when(userService.pageUsers(eq(1), eq(10), eq("uidq"), eq("nameq"), eq("phoneq"), eq("mailq"), eq(2), eq(0)))
                .thenReturn(page);

        mockMvc.perform(get("/api/user/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("uid", "uidq")
                        .param("userName", "nameq")
                        .param("userPhone", "phoneq")
                        .param("userEmail", "mailq")
                        .param("userRole", "2")
                        .param("userStatus", "0")
                        .header("Authorization", bearerForRole(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void login_success_returns200() throws Exception {
        UserVO vo = UserVO.builder().id(1L).uid("u1").userName("n").token("t").build();
        when(userService.login(any(UserLoginDTO.class))).thenReturn(vo);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CAPTCHA_SESSION_KEY, "a1b2");
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUid("u1");
        dto.setUserPassword("secret12");
        dto.setCaptchaCode("A1B2");

        mockMvc.perform(post("/api/user/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("t"));
    }

    @Test
    void login_failure_returnsBusinessError() throws Exception {
        when(userService.login(any(UserLoginDTO.class)))
                .thenThrow(new BusinessException("账号或密码错误"));

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUid("u1");
        dto.setUserPassword("wrong");
        dto.setCaptchaCode("a1b2");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CAPTCHA_SESSION_KEY, "a1b2");

        mockMvc.perform(post("/api/user/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void register_success_returns200() throws Exception {
        doNothing().when(userService).register(any(UserRegisterDTO.class));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CAPTCHA_SESSION_KEY, "a1b2");
        UserRegisterDTO dto = new UserRegisterDTO("u-new", "昵称", "123456", "a1b2");

        mockMvc.perform(post("/api/user/register")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void captcha_returnsImage() throws Exception {
        mockMvc.perform(get("/api/user/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
    }

    @Test
    void info_withToken_returns200() throws Exception {
        when(userService.getUserInfo(1L)).thenReturn(UserVO.builder().id(1L).uid("u1").build());

        mockMvc.perform(get("/api/user/info")
                        .header("Authorization", bearerForRole(4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uid").value("u1"));
    }

    @Test
    void info_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void update_withToken_returns200() throws Exception {
        doNothing().when(userService).updateUser(any(UserUpdateDTO.class));

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setUserName("newNick");

        mockMvc.perform(post("/api/user/update")
                        .header("Authorization", bearerForRole(4))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void changePassword_returns200() throws Exception {
        doNothing().when(userService).changePassword(any(UserPasswordDTO.class));

        UserPasswordDTO dto = new UserPasswordDTO("oldpass1", "newpass12");

        mockMvc.perform(post("/api/user/password")
                        .header("Authorization", bearerForRole(4))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void add_asAdmin_returns200() throws Exception {
        doNothing().when(userService).addUser(any(UserAddDTO.class));

        UserAddDTO dto = new UserAddDTO();
        dto.setUid("u2");
        dto.setUserName("name2");
        dto.setUserPassword("123456");
        dto.setUserRole(2);
        dto.setUserStatus(0);

        mockMvc.perform(post("/api/user/add")
                        .header("Authorization", bearerForRole(0))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void add_asNonAdmin_returns403() throws Exception {
        UserAddDTO dto = new UserAddDTO();
        dto.setUid("u2");
        dto.setUserName("name2");
        dto.setUserPassword("123456");
        dto.setUserRole(2);
        dto.setUserStatus(0);

        mockMvc.perform(post("/api/user/add")
                        .header("Authorization", bearerForRole(4))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asAdmin_returns200() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(post("/api/user/delete")
                        .param("id", "99")
                        .header("Authorization", bearerForRole(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void status_asManager_returns200() throws Exception {
        doNothing().when(userService).updateUserStatus(any(UserStatusDTO.class));

        UserStatusDTO dto = new UserStatusDTO(1L, 1);

        mockMvc.perform(post("/api/user/status")
                        .header("Authorization", bearerForRole(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void roles_returns200() throws Exception {
        when(userService.listRoles()).thenReturn(List.of(
                UserRoleVO.builder().roleId(0).roleName("系统管理员").build()));

        mockMvc.perform(get("/api/user/roles")
                        .header("Authorization", bearerForRole(4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].roleId").value(0));
    }
}
