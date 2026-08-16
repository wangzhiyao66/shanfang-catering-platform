package com.ordering.modules.order.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.service.OrderService;
import com.ordering.modules.order.vo.OrderAdminDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 后台订单列表：GET /api/admin/orders?status=&tableId= */
    @GetMapping("/admin/orders")
    public R<List<Order>> list(@RequestParam(required = false) Integer status,
                               @RequestParam(required = false) Long tableId) {
        return R.ok(orderService.adminList(RequestContext.getShopId(), status, tableId));
    }

    /** 后台接单：POST /api/admin/order/{id}/accept （已支付/待接单 → 制作中，并出库房分单） */
    @PostMapping("/admin/order/{id}/accept")
    public R<Void> accept(@PathVariable Long id) {
        orderService.acceptOrder(RequestContext.getShopId(), id);
        return R.ok();
    }

    /** 后台推进状态：POST /api/admin/order/{id}/status  { status } */
    @PostMapping("/admin/order/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        orderService.updateStatus(RequestContext.getShopId(), id, body.get("status"));
        return R.ok();
    }

    /** 后台取消订单：POST /api/admin/order/{id}/cancel */
    @PostMapping("/admin/order/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(RequestContext.getShopId(), id);
        return R.ok();
    }

    /** 后台订单详情：GET /api/admin/order/{id} */
    @GetMapping("/admin/order/{id}")
    public R<OrderAdminDetailVO> detail(@PathVariable Long id) {
        return R.ok(orderService.adminGetOrder(RequestContext.getShopId(), id));
    }
}
