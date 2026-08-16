package com.ordering.modules.kitchen.service;

import org.springframework.web.socket.WebSocketSession;

/** 后厨 KDS 实时推送：维护门店 ↔ WebSocket 连接，并广播消息。 */
public interface KitchenPushService {

    /** 连接建立：登记某门店的 WebSocket 会话 */
    void addSession(Long shopId, WebSocketSession session);

    /** 连接关闭：注销会话 */
    void removeSession(WebSocketSession session);

    /** 向指定门店广播消息（payload 任意可序列化对象） */
    void broadcast(Long shopId, Object payload);
}
