package com.ordering.modules.member.controller;

import com.ordering.common.annotation.LoginRequired;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.member.entity.Coupon;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@LoginRequired
public class MemberClientController {

    private final MemberService memberService;

    public MemberClientController(MemberService memberService) {
        this.memberService = memberService;
    }

    /** 顾客端会员档案：GET /api/client/member */
    @GetMapping("/client/member")
    public R<Member> profile() {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        return R.ok(memberService.getProfile(memberId));
    }

    /** 顾客端绑定手机号：POST /api/client/member/bind  { phone } */
    @PostMapping("/client/member/bind")
    public R<Void> bind(@RequestBody Map<String, String> body) {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        memberService.bindPhone(memberId, body.get("phone"));
        return R.ok();
    }

    /** 顾客端我的优惠券：GET /api/client/member/coupons */
    @GetMapping("/client/member/coupons")
    public R<List<Coupon>> coupons() {
        Long memberId = memberService.ensureMember(RequestContext.getOpenid(), RequestContext.getShopId()).getId();
        return R.ok(memberService.listCoupons(RequestContext.getShopId(), memberId));
    }
}
