package com.ordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 积分流水：change 正得负耗，type ∈ {consume, earn, expire}，ref_id 关联订单/活动。 */
@Data
@TableName("points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;
    @TableField("`change`")
    private Integer change;
    private String type;
    private Long refId;
}
