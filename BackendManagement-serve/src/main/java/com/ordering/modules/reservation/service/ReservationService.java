package com.ordering.modules.reservation.service;

import com.ordering.modules.reservation.dto.ReservationCreateDTO;
import com.ordering.modules.reservation.entity.DiningTable;
import com.ordering.modules.reservation.entity.Reservation;

import java.util.List;

public interface ReservationService {

    Long create(Long shopId, Long memberId, ReservationCreateDTO dto);

    /** 顾客取消（待确认/已确认 → 取消） */
    void cancel(Long shopId, Long memberId, Long id);

    List<Reservation> listMine(Long shopId, Long memberId);

    List<Reservation> adminList(Long shopId);

    /** 顾客端桌台/包间列表（按区域、座位升序） */
    List<DiningTable> listTables(Long shopId);

    /** 后台确认（待确认 → 已确认；若指定桌则标记桌台为「预定」） */
    void confirm(Long shopId, Long id);
}
