package com.ordering.modules.order.dto;

import lombok.Data;

import java.util.List;

/** 顾客端下单请求 */
@Data
public class OrderCreateDTO {
    private Integer type;        // 1堂食 2外卖 3自提
    private Long tableId;        // 堂食桌台（可空）
    private Integer peopleCount;
    private Long couponId;
    private List<OrderItemDTO> items;
}
