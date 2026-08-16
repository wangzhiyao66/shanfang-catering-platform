package com.ordering.common.interceptor;

import com.ordering.common.annotation.LoginRequired;
import com.ordering.common.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 顾客端拦截器：读取 X-Openid 写入上下文；标注 @LoginRequired 的接口强制登录。
 * 浏览菜单等公开接口不需要登录，仅带 shopId 即可。
 */
@Component
public class OpenidInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        boolean needLogin = hm.hasMethodAnnotation(LoginRequired.class)
                || hm.getBeanType().isAnnotationPresent(LoginRequired.class);

        String openid = request.getHeader("X-Openid");
        if (openid != null && !openid.isBlank()) {
            RequestContext.setOpenid(openid);
        }
        if (needLogin && (openid == null || openid.isBlank())) {
            writeUnauthorized(response, "请先授权登录");
            return false;
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse res, String msg) throws java.io.IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=utf-8");
        res.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
