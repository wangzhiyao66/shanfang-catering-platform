package com.ordering.common.interceptor;

import com.ordering.common.config.JwtProperties;
import com.ordering.common.context.RequestContext;
import com.ordering.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 后台（/api/admin）拦截器：校验 Authorization: Bearer <token>，写入 shopId/adminId。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    public JwtInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录");
            return false;
        }
        String token = auth.substring(7);
        Claims claims;
        try {
            claims = JwtUtil.parse(token, jwtProperties.getSecret());
        } catch (Exception e) {
            writeUnauthorized(response, "登录失效");
            return false;
        }
        Long shopId = claims.get("shopId", Long.class);
        Long adminId = claims.get("adminId", Long.class);
        if (shopId == null) {
            writeUnauthorized(response, "令牌无效");
            return false;
        }
        RequestContext.setShopId(shopId);
        if (adminId != null) {
            RequestContext.setAdminId(adminId);
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse res, String msg) throws java.io.IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=utf-8");
        res.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
