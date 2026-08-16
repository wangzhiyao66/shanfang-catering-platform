package com.ordering.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 * 注意：MyBatis-Plus 3.5.17 已移除 TenantLineInnerInterceptor（多租户）与 PaginationInnerInterceptor（分页）插件，
 * 因此「自动多租户注入」不再可用。本项目的多租户隔离改为在 Service 层显式携带 shop_id（见 MenuServiceImpl），
 * 更安全、不依赖插件版本。
 * 此处仅保留 3.5.17 仍支持的乐观锁插件（@Version 防并发），用于演示订单/菜品状态机并发边界。
 * Mapper 接口均标注 @Mapper，由 MyBatis 自动扫描，无需 @MapperScan（避免重复注册告警）。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
