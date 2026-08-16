package com.ordering.modules.reservation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 桌台：status 0空闲 1占用 2预定 3清洁中；current_order_id 占用时的订单；qr_token 扫码落座。
 */
@Data
@TableName("dining_table")
public class DiningTable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String tableNo;   // A01 / 包间1
    private String area;      // 大厅 / 包间 / 露台
    private Integer seats;
    private Integer status;
    private Long currentOrderId;
    private String qrToken;
    @Version
    private Integer version;  // 乐观锁（防并发改状态）
}
