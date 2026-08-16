package com.ordering.common.interceptor;

import com.ordering.common.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器：所有 /api 请求必须带 X-Shop-Id，写入 RequestContext 供多租户插件使用。
 * 缺失/非法 → 返回 401（与小程序未登录分支对齐）。OPTIONS 预检直接放行。
 */
@Component
public class ShopInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String shopId = request.getHeader("X-Shop-Id");
        if (shopId == null || !shopId.matches("\\d+")) {
            writeUnauthorized(response, "缺少合法的 X-Shop-Id");
            return false;
        }
        RequestContext.setShopId(Long.parseLong(shopId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 每次请求结束清理 ThreadLocal，防止线程复用串号
        RequestContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse res, String msg) throws java.io.IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=utf-8");
        res.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
