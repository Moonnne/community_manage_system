package edu.tut.cms.common.filter;

import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.result.Result;
import edu.tut.cms.common.utils.JwtUtils;
import edu.tut.cms.common.utils.JsonUtils;
import edu.tut.cms.pojo.entity.User;
import edu.tut.cms.server.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            String token = authHeader.trim();
            if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }
            try {
                CurrentUser tokenUser = jwtUtils.parseToken(token);
                User dbUser = userMapper.selectById(tokenUser.getId());
                if (dbUser == null) {
                    writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
                    return;
                }
                if (dbUser.getUserStatus() != null && dbUser.getUserStatus() == 2) {
                    writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "账号已被禁用");
                    return;
                }
                if (dbUser.getUserStatus() != null
                        && dbUser.getUserStatus() == 1
                        && isWriteRequest(request)
                        && !isUserProfileRequest(request)) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "账号已冻结，无法执行操作");
                    return;
                }
                CurrentUser user = CurrentUser.builder()
                        .id(dbUser.getId())
                        .uid(dbUser.getUid())
                        .userName(dbUser.getUserName())
                        .userRole(dbUser.getUserRole())
                        .userStatus(dbUser.getUserStatus())
                        .build();
                UserContext.setCurrentUser(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.debug("Token解析失败: {}", e.getMessage());
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isWriteRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return !"GET".equalsIgnoreCase(method)
                && !"HEAD".equalsIgnoreCase(method)
                && !"OPTIONS".equalsIgnoreCase(method);
    }

    private boolean isUserProfileRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/api/user/info".equals(path);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtils.toJson(Result.error(status, message)));
    }
}
