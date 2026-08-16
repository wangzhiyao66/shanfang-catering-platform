package com.ordering.modules.kitchen.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.kitchen.entity.KitchenTicket;
import com.ordering.modules.kitchen.service.KitchenService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KitchenAdminController {

    private final KitchenService kitchenService;

    public KitchenAdminController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    /** 后厨出单列表：GET /api/admin/kitchen/tickets?status= */
    @GetMapping("/admin/kitchen/tickets")
    public R<List<KitchenTicket>> tickets(@RequestParam(required = false) Integer status) {
        return R.ok(kitchenService.listTickets(RequestContext.getShopId(), status));
    }

    /** 标记某分单制作中：POST /api/admin/kitchen/ticket/{id}/start */
    @PostMapping("/admin/kitchen/ticket/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        kitchenService.startTicket(RequestContext.getShopId(), id);
        return R.ok();
    }

    /** 标记某分单完成：POST /api/admin/kitchen/ticket/{id}/done */
    @PostMapping("/admin/kitchen/ticket/{id}/done")
    public R<Void> done(@PathVariable Long id) {
        kitchenService.doneTicket(RequestContext.getShopId(), id);
        return R.ok();
    }
}
