package com.ordering.modules.marketing.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券视图对象：附带会员名与到期日展示字段。
 */
@Data
public class CouponVO {
    private Long id;
    private Long shopId;
    private Long memberId;
    private String memberName;
    private String name;
    private Integer value;        // 面额（分）
    private Integer threshold;    // 门槛（分）
    private Integer status;       // 0未用 1已用 2过期
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private String validTo;       // yyyy-MM-dd 展示用
}
