package com.ordering.modules.order.controller;

import com.ordering.common.annotation.LoginRequired;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.service.MemberService;
import com.ordering.modules.order.dto.OrderCreateDTO;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@LoginRequired
public class OrderClientController {

    private final OrderService orderService;
    private final MemberService memberService;

    public OrderClientController(OrderService orderService, MemberService memberService) {
        this.orderService = orderService;
        this.memberService = memberService;
    }

    /** 顾客端下单：POST /api/client/order */
    @PostMapping("/client/order")
    public R<Long> create(@RequestBody OrderCreateDTO dto) {
        Long shopId = RequestContext.getShopId();
        String openid = RequestContext.getOpenid();
        Member m = memberService.ensureMember(openid, shopId);
        return R.ok(orderService.createOrder(shopId, openid, dto));
    }

    /** 顾客端订单详情：GET /api/client/order/{id} */
    @GetMapping("/client/order/{id}")
    public R<Order> detail(@PathVariable Long id) {
        Long memberId = currentMemberId();
        return R.ok(orderService.getOrder(RequestContext.getShopId(), memberId, id));
    }

    /** 顾客端我的订单：GET /api/client/orders */
    @GetMapping("/client/orders")
    public R<List<Order>> mine() {
        return R.ok(orderService.listMyOrders(RequestContext.getShopId(), currentMemberId()));
    }

    /** 顾客端催菜：POST /api/client/order/{id}/urge */
    @PostMapping("/client/order/{id}/urge")
    public R<Void> urge(@PathVariable Long id) {
        orderService.urgeOrder(RequestContext.getShopId(), currentMemberId(), id);
        return R.ok();
    }

    private Long currentMemberId() {
        return memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
    }
}
