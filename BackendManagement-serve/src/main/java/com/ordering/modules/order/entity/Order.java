package com.ordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表。`order` 是 MySQL 保留字，表名用反引号包裹。
 * 状态机：待支付(0)→已支付/待接单(1)→制作中(2)→已上菜/待取餐(3)→已完成(4)；
 *        取消(9)；退款中(5)→已退款(6)；退单(7)。
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String orderNo;        // 业务单号（唯一索引，幂等）
    private Integer type;          // 1堂食 2外卖 3自提
    private Long memberId;
    private Long tableId;         // 堂食桌台（可空）
    private Integer status;       // 状态机
    private Integer peopleCount;
    private Integer totalAmount;  // 原价合计（分）
    private Integer discountAmount; // 优惠合计（分）
    private Integer payAmount;    // 实付（分）
    private Long couponId;
    private LocalDateTime paidAt;

    @Version
    private Integer version;       // 乐观锁（防重复支付/接单）

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt; // 软删（本骨架未启用逻辑删除，仅保留列）

    /** 订单明细（来自 order_item，非 DB 列；列表/详情接口回填） */
    @TableField(exist = false)
    private List<OrderItem> items;
}
