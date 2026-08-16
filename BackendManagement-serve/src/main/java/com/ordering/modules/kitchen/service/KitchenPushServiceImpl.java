package com.ordering.modules.kitchen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KitchenPushServiceImpl implements KitchenPushService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(Long shopId, WebSocketSession session) {
        session.getAttributes().put("shopId", shopId);
        sessions.computeIfAbsent(shopId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        Long shopId = (Long) session.getAttributes().get("shopId");
        if (shopId != null) {
            List<WebSocketSession> set = sessions.get(shopId);
            if (set != null) {
                set.remove(session);
            }
        }
    }

    @Override
    public void broadcast(Long shopId, Object payload) {
        List<WebSocketSession> set = sessions.get(shopId);
        if (set == null || set.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : set) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(msg);
                    } catch (Exception e) {
                        set.remove(s);
                    }
                } else {
                    set.remove(s);
                }
            }
        } catch (Exception ignored) {
            // 序列化/推送失败不影响主流程
        }
    }
}
