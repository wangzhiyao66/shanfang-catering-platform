package com.ordering.modules.kitchen;

import com.ordering.modules.kitchen.service.KitchenPushService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 后厨 KDS WebSocket 处理器：连接地址 /ws/kitchen?shopId=1（演示用，生产应改用 token 校验门店）。
 * 连接即按 shopId 登记，断线注销；实时接收订单/出单推送。
 */
public class KitchenWebSocketHandler extends TextWebSocketHandler {

    private final KitchenPushService kitchenPushService;

    public KitchenWebSocketHandler(KitchenPushService kitchenPushService) {
        this.kitchenPushService = kitchenPushService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long shopId = parseShopId(session);
        if (shopId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        kitchenPushService.addSession(shopId, session);
        session.sendMessage(new TextMessage("{\"type\":\"connected\",\"shopId\":" + shopId + "}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        kitchenPushService.removeSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // KDS 端心跳/交互可在此处理，演示版忽略
    }

    private Long parseShopId(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query == null) {
                return null;
            }
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && "shopId".equals(kv[0])) {
                    return Long.parseLong(kv[1]);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
