package com.ordering.modules.kitchen;

import com.ordering.modules.kitchen.service.KitchenPushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** 后厨 KDS WebSocket 配置：/ws/kitchen */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final KitchenPushService kitchenPushService;

    public WebSocketConfig(KitchenPushService kitchenPushService) {
        this.kitchenPushService = kitchenPushService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(kitchenWebSocketHandler(), "/ws/kitchen").setAllowedOrigins("*");
    }

    @Bean
    public KitchenWebSocketHandler kitchenWebSocketHandler() {
        return new KitchenWebSocketHandler(kitchenPushService);
    }
}
