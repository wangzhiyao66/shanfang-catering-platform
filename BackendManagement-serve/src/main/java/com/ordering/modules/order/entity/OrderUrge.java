package com.ordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 催菜记录：顾客在订单「制作中/已上菜」时点催菜，落库留痕供后厨查看。
 * 独立成表，避免改动 `order` 主表结构（对已有库更友好）。
 */
@Data
@TableName("order_urge")
public class OrderUrge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long orderId;
    private Long memberId;
    private String openid;
    private LocalDateTime createdAt;
}
