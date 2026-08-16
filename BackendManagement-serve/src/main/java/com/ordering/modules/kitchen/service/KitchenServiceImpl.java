package com.ordering.modules.kitchen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.kitchen.entity.KitchenStation;
import com.ordering.modules.kitchen.entity.KitchenTicket;
import com.ordering.modules.kitchen.mapper.KitchenStationMapper;
import com.ordering.modules.kitchen.mapper.KitchenTicketMapper;
import com.ordering.modules.menu.entity.Category;
import com.ordering.modules.menu.entity.Dish;
import com.ordering.modules.menu.mapper.CategoryMapper;
import com.ordering.modules.menu.mapper.DishMapper;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.entity.OrderItem;
import com.ordering.modules.order.mapper.OrderItemMapper;
import com.ordering.modules.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KitchenServiceImpl implements KitchenService {

    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final KitchenStationMapper kitchenStationMapper;
    private final KitchenTicketMapper kitchenTicketMapper;
    private final OrderMapper orderMapper;
    private final KitchenPushService kitchenPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KitchenServiceImpl(OrderItemMapper orderItemMapper, DishMapper dishMapper, CategoryMapper categoryMapper,
                              KitchenStationMapper kitchenStationMapper, KitchenTicketMapper kitchenTicketMapper,
                              OrderMapper orderMapper, KitchenPushService kitchenPushService) {
        this.orderItemMapper = orderItemMapper;
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.kitchenStationMapper = kitchenStationMapper;
        this.kitchenTicketMapper = kitchenTicketMapper;
        this.orderMapper = orderMapper;
        this.kitchenPushService = kitchenPushService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTicketsForOrder(Order order) {
        List<KitchenStation> stations = kitchenStationMapper.selectList(
                new LambdaQueryWrapper<KitchenStation>().eq(KitchenStation::getShopId, order.getShopId()));
        if (stations.isEmpty()) {
            return; // 未配置档口则不拆分
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        if (items.isEmpty()) {
            return;
        }
        // 按菜品分类（档口）分组
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (OrderItem it : items) {
            Dish dish = dishMapper.selectById(it.getDishId());
            String catName = "其他";
            if (dish != null && dish.getCategoryId() != null) {
                Category cat = categoryMapper.selectById(dish.getCategoryId());
                if (cat != null && cat.getName() != null) {
                    catName = cat.getName();
                }
            }
            groups.computeIfAbsent(catName, k -> new ArrayList<>())
                    .add(buildItemInfo(it));
        }

        KitchenStation defaultStation = stations.get(0);
        List<KitchenTicket> created = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : groups.entrySet()) {
            KitchenStation station = stations.stream()
                    .filter(s -> s.getName() != null && s.getName().equals(e.getKey()))
                    .findFirst().orElse(defaultStation);
            KitchenTicket t = new KitchenTicket();
            t.setShopId(order.getShopId());
            t.setOrderId(order.getId());
            t.setStationId(station.getId());
            t.setPrinterId(station.getPrinterId());
            try {
                t.setItemsJson(objectMapper.writeValueAsString(e.getValue()));
            } catch (Exception ex) {
                t.setItemsJson("[]");
            }
            t.setStatus(0);
            t.setPushedAt(LocalDateTime.now());
            kitchenTicketMapper.insert(t);
            created.add(t);
        }
        // 推送后厨
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "newTickets");
        payload.put("orderId", order.getId());
        payload.put("count", created.size());
        kitchenPushService.broadcast(order.getShopId(), payload);
    }

    @Override
    public List<KitchenTicket> listTickets(Long shopId, Integer status) {
        LambdaQueryWrapper<KitchenTicket> q = new LambdaQueryWrapper<KitchenTicket>().eq(KitchenTicket::getShopId, shopId);
        if (status != null) {
            q.eq(KitchenTicket::getStatus, status);
        }
        return kitchenTicketMapper.selectList(q.orderByAsc(KitchenTicket::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTicket(Long shopId, Long ticketId) {
        KitchenTicket t = require(shopId, ticketId);
        if (t.getStatus() != 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "仅待做的分单可开始制作");
        }
        t.setStatus(1);
        kitchenTicketMapper.updateById(t);
        broadcastTicket(shopId, t, "ticketStart");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doneTicket(Long shopId, Long ticketId) {
        KitchenTicket t = require(shopId, ticketId);
        if (t.getStatus() == 2) {
            return;
        }
        t.setStatus(2);
        t.setDoneAt(LocalDateTime.now());
        kitchenTicketMapper.updateById(t);
        broadcastTicket(shopId, t, "ticketDone");
        // 整单分单全部完成 → 推进订单到「已上菜/待取餐(3)」
        long remaining = kitchenTicketMapper.selectCount(new LambdaQueryWrapper<KitchenTicket>()
                .eq(KitchenTicket::getOrderId, t.getOrderId())
                .ne(KitchenTicket::getStatus, 2));
        if (remaining == 0) {
            Order o = orderMapper.selectById(t.getOrderId());
            if (o != null && o.getStatus() == 2) {
                Order upd = new Order();
                upd.setId(o.getId());
                upd.setStatus(3);
                upd.setVersion(o.getVersion());
                orderMapper.updateById(upd);
                Map<String, Object> p = new HashMap<>();
                p.put("type", "orderStatus");
                p.put("orderId", o.getId());
                p.put("status", 3);
                kitchenPushService.broadcast(shopId, p);
            }
        }
    }

    // ===== 内部 =====

    private KitchenTicket require(Long shopId, Long ticketId) {
        KitchenTicket t = kitchenTicketMapper.selectOne(new LambdaQueryWrapper<KitchenTicket>()
                .eq(KitchenTicket::getId, ticketId).eq(KitchenTicket::getShopId, shopId));
        if (t == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "分单不存在");
        }
        return t;
    }

    private void broadcastTicket(Long shopId, KitchenTicket t, String type) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", type);
        p.put("ticketId", t.getId());
        p.put("orderId", t.getOrderId());
        p.put("status", t.getStatus());
        kitchenPushService.broadcast(shopId, p);
    }

    private Map<String, Object> buildItemInfo(OrderItem it) {
        Map<String, Object> m = new HashMap<>();
        m.put("dishName", it.getDishName());
        m.put("qty", it.getQty());
        m.put("specsJson", it.getSpecsJson());
        m.put("remark", it.getRemark());
        return m;
    }
}
