package com.ordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券：发给会员的抵扣券。value=面额(分)，threshold=使用门槛(分,0=无门槛)，
 * status 0未使用 1已使用 2已过期。validTo 为展示用到期日(yyyy-MM-dd)，非 DB 列。
 */
@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long memberId;
    private String name;        // 券名，如「满50减10」
    private Integer value;      // 面额（分）：抵扣金额
    private Integer threshold;  // 使用门槛（分）：订单实付满 threshold 可用，0=无门槛
    private Integer status;     // 0未用 1已用 2过期
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String validTo;     // 展示用到期日 yyyy-MM-dd
}
