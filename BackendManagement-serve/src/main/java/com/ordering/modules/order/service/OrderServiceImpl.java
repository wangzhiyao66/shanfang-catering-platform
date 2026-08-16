package com.ordering.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.kitchen.service.KitchenPushService;
import com.ordering.modules.kitchen.service.KitchenService;
import com.ordering.modules.member.entity.Coupon;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.mapper.CouponMapper;
import com.ordering.modules.member.service.MemberService;
import com.ordering.modules.menu.entity.Dish;
import com.ordering.modules.menu.entity.DishSpec;
import com.ordering.modules.menu.mapper.DishMapper;
import com.ordering.modules.menu.mapper.DishSpecMapper;
import com.ordering.modules.order.dto.OrderCreateDTO;
import com.ordering.modules.order.dto.OrderItemDTO;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.entity.OrderItem;
import com.ordering.modules.order.entity.OrderUrge;
import com.ordering.modules.order.mapper.OrderItemMapper;
import com.ordering.modules.order.mapper.OrderMapper;
import com.ordering.modules.order.mapper.OrderUrgeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderUrgeMapper orderUrgeMapper;
    private final DishMapper dishMapper;
    private final DishSpecMapper dishSpecMapper;
    private final CouponMapper couponMapper;
    private final MemberService memberService;
    private final KitchenService kitchenService;
    private final KitchenPushService kitchenPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, OrderUrgeMapper orderUrgeMapper,
                            DishMapper dishMapper, DishSpecMapper dishSpecMapper, CouponMapper couponMapper,
                            MemberService memberService, KitchenService kitchenService, KitchenPushService kitchenPushService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderUrgeMapper = orderUrgeMapper;
        this.dishMapper = dishMapper;
        this.dishSpecMapper = dishSpecMapper;
        this.couponMapper = couponMapper;
        this.memberService = memberService;
        this.kitchenService = kitchenService;
        this.kitchenPushService = kitchenPushService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long shopId, String openid, OrderCreateDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "订单不能为空");
        }
        Member member = memberService.ensureMember(openid, shopId);

        List<OrderItem> items = new ArrayList<>();
        int total = 0;
        for (OrderItemDTO it : dto.getItems()) {
            if (it.getDishId() == null || it.getQty() == null || it.getQty() <= 0) {
                throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "菜品项无效");
            }
            Dish dish = dishMapper.selectOne(new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getId, it.getDishId()).eq(Dish::getShopId, shopId));
            if (dish == null || (dish.getStatus() != null && dish.getStatus() == 0)) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "菜品不存在或已下架");
            }
            // 规格加价：按提交的 specsJson 中命中的 dish_spec 累加 price_delta
            int unitPrice = dish.getPrice() + specDelta(shopId, dish.getId(), it.getSpecsJson());

            OrderItem oi = new OrderItem();
            oi.setDishId(dish.getId());
            oi.setSkuId(it.getSkuId());
            oi.setDishName(dish.getName());
            oi.setQty(it.getQty());
            oi.setUnitPrice(unitPrice);
            oi.setSpecsJson(it.getSpecsJson());
            oi.setRemark(it.getRemark());
            items.add(oi);
            total += unitPrice * it.getQty();
        }

        // 会员等级折扣（服务端计算）
        BigDecimal discount = memberService.discountOf(member.getLevelId());
        int discountAmount = (int) Math.round(total * BigDecimal.ONE.subtract(discount).doubleValue());

        // 优惠券抵扣（服务端校验归属/未用/门槛后扣减，并标记已用）
        int couponDiscount = 0;
        if (dto.getCouponId() != null) {
            Coupon coupon = couponMapper.selectOne(new LambdaQueryWrapper<Coupon>()
                    .eq(Coupon::getId, dto.getCouponId())
                    .eq(Coupon::getShopId, shopId)
                    .eq(Coupon::getMemberId, member.getId()));
            if (coupon == null) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "优惠券不存在");
            }
            if (coupon.getStatus() == null || coupon.getStatus() != 0) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "优惠券已使用或已失效");
            }
            if (coupon.getThreshold() != null && coupon.getThreshold() > 0 && total < coupon.getThreshold()) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "未满足优惠券使用门槛");
            }
            couponDiscount = coupon.getValue() == null ? 0 : coupon.getValue();
            Coupon used = new Coupon();
            used.setId(coupon.getId());
            used.setStatus(1);
            used.setUsedAt(LocalDateTime.now());
            couponMapper.updateById(used);
        }

        int payAmount = Math.max(0, total - discountAmount - couponDiscount);

        Order o = new Order();
        o.setShopId(shopId);
        o.setOrderNo(genOrderNo(shopId));
        o.setType(dto.getType() == null ? 1 : dto.getType());
        o.setMemberId(member.getId());
        o.setTableId(dto.getTableId());
        o.setStatus(0);
        o.setPeopleCount(dto.getPeopleCount() == null ? 1 : dto.getPeopleCount());
        o.setTotalAmount(total);
        o.setDiscountAmount(discountAmount + couponDiscount);
        o.setPayAmount(payAmount);
        o.setCouponId(dto.getCouponId());
        o.setVersion(0);
        orderMapper.insert(o);

        for (OrderItem oi : items) {
            oi.setOrderId(o.getId());
            orderItemMapper.insert(oi);
        }
        return o.getId();
    }

    @Override
    public Order getOrder(Long shopId, Long memberId, Long id) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, id).eq(Order::getShopId, shopId).eq(Order::getMemberId, memberId));
        if (o == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单不存在");
        }
        o.setItems(orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())));
        return o;
    }

    @Override
    public List<Order> listMyOrders(Long shopId, Long memberId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId).eq(Order::getMemberId, memberId)
                .orderByDesc(Order::getCreatedAt));
        if (!orders.isEmpty()) {
            List<Long> ids = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, ids));
            Map<Long, List<OrderItem>> byOrder = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));
            orders.forEach(o -> o.setItems(byOrder.getOrDefault(o.getId(), new ArrayList<>())));
        }
        return orders;
    }

    @Override
    public List<Order> adminList(Long shopId, Integer status, Long tableId) {
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId);
        if (status != null) {
            q.eq(Order::getStatus, status);
        }
        if (tableId != null) {
            q.eq(Order::getTableId, tableId);
        }
        return orderMapper.selectList(q.orderByDesc(Order::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long shopId, Long id) {
        Order o = requireOrder(shopId, id);
        if (o.getStatus() != 1) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "仅「已支付/待接单」的订单可接单");
        }
        Order upd = new Order();
        upd.setId(id);
        upd.setStatus(2); // 制作中
        upd.setVersion(o.getVersion());
        if (orderMapper.updateById(upd) == 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单状态已变化，请刷新");
        }
        kitchenService.createTicketsForOrder(o);
        broadcastStatus(shopId, id, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long shopId, Long id, Integer targetStatus) {
        if (targetStatus == null) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "目标状态为空");
        }
        Order o = requireOrder(shopId, id);
        if (!canTransit(o.getStatus(), targetStatus)) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(),
                    "非法状态流转：" + o.getStatus() + " → " + targetStatus);
        }
        Order upd = new Order();
        upd.setId(id);
        upd.setStatus(targetStatus);
        upd.setVersion(o.getVersion());
        if (targetStatus == 1 && o.getPaidAt() == null) {
            upd.setPaidAt(LocalDateTime.now());
        }
        if (orderMapper.updateById(upd) == 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单状态已变化，请刷新");
        }
        broadcastStatus(shopId, id, targetStatus);
    }

    @Override
    public void cancelOrder(Long shopId, Long id) {
        updateStatus(shopId, id, 9);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urgeOrder(Long shopId, Long memberId, Long orderId) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getShopId, shopId).eq(Order::getMemberId, memberId));
        if (o == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单不存在");
        }
        if (o.getStatus() != 2 && o.getStatus() != 3) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "仅「制作中/已上菜」订单可催菜");
        }
        OrderUrge u = new OrderUrge();
        u.setShopId(shopId);
        u.setOrderId(orderId);
        u.setMemberId(memberId);
        u.setCreatedAt(LocalDateTime.now());
        orderUrgeMapper.insert(u);
    }

    // ===== 内部辅助 =====

    /** 解析前端提交的 specsJson（数组名 或 {组:选项} 对象），累加命中的 dish_spec.price_delta */
    private int specDelta(Long shopId, Long dishId, String specsJson) {
        if (specsJson == null || specsJson.trim().isEmpty()) {
            return 0;
        }
        List<String> names;
        try {
            Object parsed = objectMapper.readValue(specsJson, Object.class);
            if (parsed instanceof List) {
                names = ((List<?>) parsed).stream().map(String::valueOf).collect(Collectors.toList());
            } else if (parsed instanceof Map) {
                names = ((Map<?, ?>) parsed).values().stream().map(String::valueOf).collect(Collectors.toList());
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
        if (names.isEmpty()) {
            return 0;
        }
        int delta = 0;
        for (String name : names) {
            DishSpec sp = dishSpecMapper.selectOne(new LambdaQueryWrapper<DishSpec>()
                    .eq(DishSpec::getDishId, dishId)
                    .eq(DishSpec::getShopId, shopId)
                    .eq(DishSpec::getName, name));
            if (sp != null && sp.getPriceDelta() != null) {
                delta += sp.getPriceDelta();
            }
        }
        return delta;
    }

    private Order requireOrder(Long shopId, Long id) {
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, id).eq(Order::getShopId, shopId));
        if (o == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单不存在");
        }
        return o;
    }

    /** 状态机合法流转：待支付(0)→已支付(1)→制作中(2)→已上菜(3)→完成(4)；取消(9)/退款(5→6)/退单(7) */
    private boolean canTransit(int from, int to) {
        if (from == to) {
            return false;
        }
        switch (from) {
            case 0: return to == 1 || to == 9;
            case 1: return to == 2 || to == 5 || to == 9;
            case 2: return to == 3 || to == 7;
            case 3: return to == 4;
            case 5: return to == 6;
            default: return false; // 4/6/7/9 终态
        }
    }

    private void broadcastStatus(Long shopId, Long orderId, int status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "orderStatus");
        payload.put("orderId", orderId);
        payload.put("status", status);
        kitchenPushService.broadcast(shopId, payload);
    }

    private String genOrderNo(Long shopId) {
        return "O" + shopId + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
