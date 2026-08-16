package com.ordering.modules.order.dto;

import lombok.Data;

/** 下单明细项（前端提交） */
@Data
public class OrderItemDTO {
    private Long dishId;
    private Long skuId;
    private Integer qty;
    private String specsJson;
    private String remark;
}
