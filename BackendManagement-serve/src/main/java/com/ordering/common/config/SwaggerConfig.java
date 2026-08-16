package com.ordering.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger 元信息。访问 /swagger-ui.html 查看接口文档（三端联调单一来源）。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderingApi() {
        return new OpenAPI().info(new Info()
                .title("点餐小程序后端 API")
                .version("v1")
                .description("小程序顾客端 /api/client 与商家后台 /api/admin 共用同一服务"));
    }
}
