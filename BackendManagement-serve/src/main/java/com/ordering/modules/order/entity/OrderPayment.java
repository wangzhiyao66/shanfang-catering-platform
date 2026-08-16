package com.ordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付记录（微信支付流水）。pay_no 唯一索引，做幂等键。
 * 状态：0待支付 1成功 2失败 3退款。
 */
@Data
@TableName("order_payment")
public class OrderPayment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String payNo;     // 微信支付流水号（幂等键，通常用 order_no）
    private String channel;   // wechat
    private Integer amount;  // 金额（分）
    private Integer status;  // 0/1/2/3
    private LocalDateTime paidAt;
    private String refundNo; // 退款单号
}
