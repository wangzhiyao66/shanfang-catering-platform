package com.ordering.modules.reservation.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.reservation.entity.Reservation;
import com.ordering.modules.reservation.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationAdminController {

    private final ReservationService reservationService;

    public ReservationAdminController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** 后台预订列表：GET /api/admin/reservations */
    @GetMapping("/admin/reservations")
    public R<List<Reservation>> list() {
        return R.ok(reservationService.adminList(RequestContext.getShopId()));
    }

    /** 后台确认预订：POST /api/admin/reservation/{id}/confirm */
    @PostMapping("/admin/reservation/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        reservationService.confirm(RequestContext.getShopId(), id);
        return R.ok();
    }
}
