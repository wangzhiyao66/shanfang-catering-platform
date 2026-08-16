package com.ordering.modules.reservation.dto;

import lombok.Data;

/** 顾客端提交预订 */
@Data
public class ReservationCreateDTO {
    private Long tableId;       // 指定桌/包间（可空=不指定）
    private String date;        // yyyy-MM-dd
    private String timeSlot;    // 时段
    private Integer partySize;
    private Integer deposit;    // 订金（分，可空）
}
