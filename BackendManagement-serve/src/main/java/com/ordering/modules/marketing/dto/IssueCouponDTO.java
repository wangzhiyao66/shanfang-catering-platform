package com.ordering.modules.marketing.dto;

import lombok.Data;

/**
 * 发放优惠券入参：指定会员 + 面额/门槛/有效期天数。
 */
@Data
public class IssueCouponDTO {
    private Long memberId;
    private String name;
    private Integer value;       // 面额（分）
    private Integer threshold;   // 门槛（分），默认 0
    private Integer validDays;   // 有效期天数，默认 30
}
