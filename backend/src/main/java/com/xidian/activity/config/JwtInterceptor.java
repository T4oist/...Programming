package com.xidian.activity.config;

import com.xidian.activity.common.ErrorCode;
import com.xidian.activity.common.BusinessException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端登录拦截器：校验 Authorization: Bearer <jwt>
 * 校验通过后把 adminId 放入 request attribute
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String ATTR_ADMIN_ID = "adminId";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 放行 CORS 预检
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            long adminId = Long.parseLong(jwtUtil.parse(auth.substring(7)).getSubject());
            request.setAttribute(ATTR_ADMIN_ID, adminId);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
