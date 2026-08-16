package com.ordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 会员等级：discount 折扣（0.90 表示 9 折），threshold 升级消费门槛（分）。 */
@Data
@TableName("member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;
    private BigDecimal discount;
    private Integer threshold;
}
