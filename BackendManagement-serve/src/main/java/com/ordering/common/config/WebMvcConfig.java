package com.ordering.common.config;

import com.ordering.common.interceptor.JwtInterceptor;
import com.ordering.common.interceptor.OpenidInterceptor;
import com.ordering.common.interceptor.ShopInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册顺序：Shop(租户) → Openid(顾客登录) → Jwt(后台)。
 * ShopInterceptor 排最前，确保后续拦截器与 MyBatis-Plus 多租户都能拿到 shopId。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ShopInterceptor shopInterceptor;
    private final OpenidInterceptor openidInterceptor;
    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(ShopInterceptor shopInterceptor, OpenidInterceptor openidInterceptor, JwtInterceptor jwtInterceptor) {
        this.shopInterceptor = shopInterceptor;
        this.openidInterceptor = openidInterceptor;
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(shopInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/error",
                        "/api/client/pay/notify"); // 微信支付回调不带 X-Shop-Id，单独排除

        registry.addInterceptor(openidInterceptor)
                .addPathPatterns("/api/client/**")
                .excludePathPatterns("/api/client/pay/notify");

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login"); // 登录接口本身不校验 JWT
    }
}
