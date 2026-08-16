package com.ordering.modules.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.modules.dashboard.service.DashboardService;
import com.ordering.modules.dashboard.vo.DashboardVO;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.mapper.MemberMapper;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.entity.OrderItem;
import com.ordering.modules.order.mapper.OrderItemMapper;
import com.ordering.modules.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;

    public DashboardServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, MemberMapper memberMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    public DashboardVO adminDashboard(Long shopId) {
        DashboardVO vo = new DashboardVO();
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId));

        LocalDate today = LocalDate.now();
        int todayRevenue = 0;
        int todayOrderCount = 0;
        int pendingOrders = 0;
        Map<Integer, Integer> typeCount = new HashMap<>();

        for (Order o : orders) {
            LocalDate d = o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate();
            boolean cancelled = o.getStatus() != null && o.getStatus() == 9;
            boolean refund = o.getStatus() != null && (o.getStatus() == 5 || o.getStatus() == 6 || o.getStatus() == 7);
            if (d != null && d.equals(today)) {
                if (!cancelled) {
                    todayOrderCount++;
                    if (!refund) todayRevenue += o.getPayAmount() == null ? 0 : o.getPayAmount();
                }
            }
            if (o.getStatus() != null && o.getStatus() >= 0 && o.getStatus() <= 3) pendingOrders++;
            typeCount.merge(o.getType(), 1, Integer::sum);
        }
        vo.setTodayRevenue(todayRevenue);
        vo.setTodayOrderCount(todayOrderCount);
        vo.setPendingOrders(pendingOrders);
        vo.setTotalMembers(memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getShopId, shopId)).intValue());

        // 近 7 日营收趋势
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int amt = 0, cnt = 0;
            for (Order o : orders) {
                LocalDate d = o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate();
                if (d != null && d.equals(day)) {
                    boolean cancelled = o.getStatus() != null && o.getStatus() == 9;
                    boolean refund = o.getStatus() != null && (o.getStatus() == 5 || o.getStatus() == 6 || o.getStatus() == 7);
                    if (!cancelled) {
                        cnt++;
                        if (!refund) amt += o.getPayAmount() == null ? 0 : o.getPayAmount();
                    }
                }
            }
            DashboardVO.DayPoint dp = new DashboardVO.DayPoint();
            dp.setDate(day.format(fmt));
            dp.setAmount(amt);
            dp.setOrderCount(cnt);
            vo.getWeekRevenue().add(dp);
        }

        // 订单类型分布
        typeCount.forEach((k, v) -> {
            DashboardVO.TypeCount tc = new DashboardVO.TypeCount();
            tc.setType(k);
            tc.setCount(v);
            vo.getOrderTypeDist().add(tc);
        });

        // 热销 TOP5
        if (!orders.isEmpty()) {
            List<Long> ids = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, ids));
            Map<String, DashboardVO.DishRank> rankMap = new LinkedHashMap<>();
            for (OrderItem it : items) {
                String name = it.getDishName() == null ? "未知" : it.getDishName();
                DashboardVO.DishRank r = rankMap.computeIfAbsent(name, k -> {
                    DashboardVO.DishRank x = new DashboardVO.DishRank();
                    x.setDishName(k);
                    x.setQty(0);
                    x.setAmount(0);
                    return x;
                });
                int qty = it.getQty() == null ? 0 : it.getQty();
                int price = it.getUnitPrice() == null ? 0 : it.getUnitPrice();
                r.setQty(r.getQty() + qty);
                r.setAmount(r.getAmount() + price * qty);
            }
            vo.setTopDishes(rankMap.values().stream()
                    .sorted((a, b) -> Integer.compare(b.getQty(), a.getQty()))
                    .limit(5).collect(Collectors.toList()));
        }
        return vo;
    }
}
