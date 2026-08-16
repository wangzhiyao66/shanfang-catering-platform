package com.ordering.modules.reservation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预订/订座：date 日期、time_slot 时段、party_size 人数、deposit 订金（分）。
 * status 0待确认 1已确认 2到店 3取消 4爽约。
 */
@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long memberId;
    private Long tableId;     // 指定桌/包间
    @TableField("`date`")
    private LocalDate date;
    private String timeSlot;  // 时段，如 "18:00-19:00"
    private Integer partySize;
    private Integer deposit;  // 订金（分）
    private Integer status;
}
