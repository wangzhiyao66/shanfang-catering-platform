package com.ordering.modules.report.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据报表视图对象：营收/订单/客单价趋势 + 订单类型占比 + 热销菜品 + 汇总。
 * 金额单位为「分」。
 */
@Data
public class ReportVO {

    private List<TrendPoint> revenueTrend = new ArrayList<>(); // 每日趋势
    private List<TypeCount> typeDist = new ArrayList<>();       // 订单类型占比
    private List<DishRank> topDishes = new ArrayList<>();        // 热销菜品 TOP10
    private Summary summary = new Summary();                     // 汇总

    @Data
    public static class TrendPoint {
        private String date;     // yyyy-MM-dd
        private int revenue;     // 营收（分）
        private int orderCount;
        private int avgAmount;   // 客单价（分）
    }

    @Data
    public static class TypeCount {
        private Integer type;
        private int count;
        private int amount;      // 该类型营收（分）
    }

    @Data
    public static class DishRank {
        private String dishName;
        private int qty;
        private int amount;      // 销售额（分）
    }

    @Data
    public static class Summary {
        private int totalRevenue;   // 总营收（分）
        private int totalOrders;    // 总订单数（不含取消）
        private int avgOrderValue;  // 客单价（分）
        private int refundAmount;   // 退款金额（分）
    }
}
