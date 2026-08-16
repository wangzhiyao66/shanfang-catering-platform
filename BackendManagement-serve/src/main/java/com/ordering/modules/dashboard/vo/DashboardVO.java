package com.ordering.modules.dashboard.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台视图对象：核心指标 + 近 7 日营收趋势 + 订单类型分布 + 热销 TOP。
 * 金额单位为「分」。
 */
@Data
public class DashboardVO {

    private int todayRevenue;     // 今日营收（分）
    private int todayOrderCount; // 今日订单数（不含已取消）
    private int pendingOrders;    // 进行中订单（状态 0~3）
    private int totalMembers;     // 会员总数

    private List<DayPoint> weekRevenue = new ArrayList<>();   // 近 7 日
    private List<TypeCount> orderTypeDist = new ArrayList<>(); // 订单类型分布
    private List<DishRank> topDishes = new ArrayList<>();      // 热销 TOP5

    @Data
    public static class DayPoint {
        private String date;       // MM-dd
        private int amount;        // 营收（分）
        private int orderCount;
    }

    @Data
    public static class TypeCount {
        private Integer type;
        private int count;
    }

    @Data
    public static class DishRank {
        private String dishName;
        private int qty;
        private int amount;        // 销售额（分）
    }
}
