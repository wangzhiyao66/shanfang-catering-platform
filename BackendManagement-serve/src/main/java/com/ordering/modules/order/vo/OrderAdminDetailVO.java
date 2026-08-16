package com.ordering.modules.order.vo;

import com.ordering.modules.order.entity.OrderItem;
import com.ordering.modules.order.entity.OrderPayment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台订单详情视图对象：聚合订单主信息、菜品明细、支付单、会员名、桌台号。
 */
@Data
public class OrderAdminDetailVO {

    private Long id;
    private String orderNo;
    private Integer type;          // 1堂食 2外卖 3自提
    private Long memberId;
    private Long tableId;
    private Integer status;        // 状态机
    private Integer peopleCount;
    private Integer totalAmount;   // 分
    private Integer discountAmount;// 分
    private Integer payAmount;     // 分
    private Long couponId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    private String memberName;     // 展示用
    private String tableNo;        // 展示用
    private List<OrderItem> items; // 菜品明细
    private OrderPayment payment;  // 支付单（可空）
}
