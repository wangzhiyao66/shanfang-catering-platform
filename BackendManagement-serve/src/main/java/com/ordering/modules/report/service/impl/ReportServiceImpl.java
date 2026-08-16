package com.ordering.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.entity.OrderItem;
import com.ordering.modules.order.mapper.OrderItemMapper;
import com.ordering.modules.order.mapper.OrderMapper;
import com.ordering.modules.report.service.ReportService;
import com.ordering.modules.report.vo.ReportVO;
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
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public ReportServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public ReportVO adminReport(Long shopId, Integer days) {
        int range = (days == null || days < 1) ? 30 : Math.min(days, 365);
        ReportVO vo = new ReportVO();
        List<Order> all = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getShopId, shopId));

        // 仅统计时间窗内的订单
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(range - 1L);
        List<Order> orders = all.stream()
                .filter(o -> o.getCreatedAt() != null
                        && !o.getCreatedAt().toLocalDate().isBefore(start)
                        && !o.getCreatedAt().toLocalDate().isAfter(today))
                .collect(Collectors.toList());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<LocalDate, List<Order>> byDay = new LinkedHashMap<>();
        for (int i = 0; i < range; i++) byDay.put(start.plusDays(i), new ArrayList<>());
        for (Order o : orders) {
            LocalDate d = o.getCreatedAt().toLocalDate();
            byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(o);
        }

        int totalRevenue = 0, totalOrders = 0, refundAmount = 0;
        Map<Integer, TypeAgg> typeAgg = new HashMap<>();

        for (Map.Entry<LocalDate, List<Order>> e : byDay.entrySet()) {
            int amt = 0, cnt = 0;
            for (Order o : e.getValue()) {
                boolean cancelled = o.getStatus() != null && o.getStatus() == 9;
                boolean refund = o.getStatus() != null && (o.getStatus() == 5 || o.getStatus() == 6 || o.getStatus() == 7);
                if (!cancelled) {
                    cnt++;
                    if (!refund) {
                        int pay = o.getPayAmount() == null ? 0 : o.getPayAmount();
                        amt += pay;
                        totalRevenue += pay;
                    } else {
                        refundAmount += o.getPayAmount() == null ? 0 : o.getPayAmount();
                    }
                }
                if (o.getStatus() != null && o.getStatus() != 9) {
                    TypeAgg ta = typeAgg.computeIfAbsent(o.getType(), k -> new TypeAgg());
                    ta.count++;
                    if (!refund) ta.amount += o.getPayAmount() == null ? 0 : o.getPayAmount();
                }
            }
            ReportVO.TrendPoint tp = new ReportVO.TrendPoint();
            tp.setDate(e.getKey().format(fmt));
            tp.setRevenue(amt);
            tp.setOrderCount(cnt);
            tp.setAvgAmount(cnt > 0 ? amt / cnt : 0);
            vo.getRevenueTrend().add(tp);
            totalOrders += cnt;
        }

        vo.getSummary().setTotalRevenue(totalRevenue);
        vo.getSummary().setTotalOrders(totalOrders);
        vo.getSummary().setAvgOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0);
        vo.getSummary().setRefundAmount(refundAmount);

        typeAgg.forEach((k, v) -> {
            ReportVO.TypeCount tc = new ReportVO.TypeCount();
            tc.setType(k);
            tc.setCount(v.count);
            tc.setAmount(v.amount);
            vo.getTypeDist().add(tc);
        });

        // 热销菜品 TOP10（统计窗内）
        if (!orders.isEmpty()) {
            List<Long> ids = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, ids));
            Map<String, ReportVO.DishRank> rankMap = new LinkedHashMap<>();
            for (OrderItem it : items) {
                String name = it.getDishName() == null ? "未知" : it.getDishName();
                ReportVO.DishRank r = rankMap.computeIfAbsent(name, k -> {
                    ReportVO.DishRank x = new ReportVO.DishRank();
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
                    .limit(10).collect(Collectors.toList()));
        }
        return vo;
    }

    private static class TypeAgg {
        int count;
        int amount;
    }
}
