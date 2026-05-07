package edu.tut.cms.common.aop;

import edu.tut.cms.common.context.CurrentUser;
import edu.tut.cms.common.context.UserContext;
import edu.tut.cms.common.exception.ForbiddenException;
import edu.tut.cms.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
@Slf4j
public class AuthAspect {

    private static final Map<Integer, Integer> PERMISSION_MAP = Map.of(
            0, 3,
            1, 2,
            2, 1,
            3, 1,
            4, 0,
            5, 0
    );

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp, RequirePermission requirePermission) throws Throwable {
        CurrentUser user = UserContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("未登录");
        }
        int level = PERMISSION_MAP.getOrDefault(user.getUserRole(), 0);
        if (level < requirePermission.minLevel()) {
            throw new ForbiddenException("权限不足");
        }
        return pjp.proceed();
    }

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint pjp, RequireRole requireRole) throws Throwable {
        CurrentUser user = UserContext.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("未登录");
        }
        int[] allowed = requireRole.value();
        boolean match = false;
        for (int r : allowed) {
            if (r == user.getUserRole()) {
                match = true;
                break;
            }
        }
        if (!match) {
            throw new ForbiddenException("无权访问该功能");
        }
        return pjp.proceed();
    }
}
