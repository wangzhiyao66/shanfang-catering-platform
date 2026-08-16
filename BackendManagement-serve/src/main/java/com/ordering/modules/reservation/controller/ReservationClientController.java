package com.ordering.modules.reservation.controller;

import com.ordering.common.annotation.LoginRequired;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.member.service.MemberService;
import com.ordering.modules.reservation.dto.ReservationCreateDTO;
import com.ordering.modules.reservation.entity.DiningTable;
import com.ordering.modules.reservation.entity.Reservation;
import com.ordering.modules.reservation.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@LoginRequired
public class ReservationClientController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationClientController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    /** 顾客端提交预订：POST /api/client/reservation */
    @PostMapping("/client/reservation")
    public R<Long> create(@RequestBody ReservationCreateDTO dto) {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        return R.ok(reservationService.create(RequestContext.getShopId(), memberId, dto));
    }

    /** 顾客端我的预订：GET /api/client/reservations */
    @GetMapping("/client/reservations")
    public R<List<Reservation>> mine() {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        return R.ok(reservationService.listMine(RequestContext.getShopId(), memberId));
    }

    /** 顾客端取消预订：POST /api/client/reservation/{id}/cancel */
    @PostMapping("/client/reservation/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        reservationService.cancel(RequestContext.getShopId(), memberId, id);
        return R.ok();
    }

    /** 顾客端桌台/包间列表：GET /api/client/tables */
    @GetMapping("/client/tables")
    public R<List<DiningTable>> tables() {
        return R.ok(reservationService.listTables(RequestContext.getShopId()));
    }
}
