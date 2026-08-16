package com.ordering.modules.marketing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.marketing.dto.IssueCouponDTO;
import com.ordering.modules.member.entity.Coupon;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.mapper.CouponMapper;
import com.ordering.modules.member.mapper.MemberMapper;
import com.ordering.modules.marketing.vo.CouponVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MarketingController {

    private final CouponMapper couponMapper;
    private final MemberMapper memberMapper;

    public MarketingController(CouponMapper couponMapper, MemberMapper memberMapper) {
        this.couponMapper = couponMapper;
        this.memberMapper = memberMapper;
    }

    /** 优惠券列表（带会员名）：GET /api/admin/marketing/coupons?status= */
    @GetMapping("/admin/marketing/coupons")
    public R<List<CouponVO>> list(@RequestParam(required = false) Integer status) {
        Long shopId = RequestContext.getShopId();
        List<Coupon> list = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .eq(status != null, Coupon::getStatus, status)
                .orderByDesc(Coupon::getCreatedAt));
        Map<Long, String> nameMap = memberMapper.selectList(
                        new LambdaQueryWrapper<Member>().eq(Member::getShopId, shopId))
                .stream().collect(Collectors.toMap(Member::getId, Member::getNickname, (a, b) -> a));
        List<CouponVO> vos = list.stream().map(c -> {
            CouponVO v = new CouponVO();
            v.setId(c.getId());
            v.setShopId(c.getShopId());
            v.setMemberId(c.getMemberId());
            v.setMemberName(nameMap.get(c.getMemberId()));
            v.setName(c.getName());
            v.setValue(c.getValue());
            v.setThreshold(c.getThreshold());
            v.setStatus(c.getStatus());
            v.setStartTime(c.getStartTime());
            v.setEndTime(c.getEndTime());
            v.setUsedAt(c.getUsedAt());
            v.setCreatedAt(c.getCreatedAt());
            v.setValidTo(c.getEndTime() == null ? null : c.getEndTime().toLocalDate().toString());
            return v;
        }).collect(Collectors.toList());
        return R.ok(vos);
    }

    /** 发放优惠券：POST /api/admin/marketing/coupons */
    @PostMapping("/admin/marketing/coupons")
    public R<Long> issue(@RequestBody IssueCouponDTO dto) {
        Long shopId = RequestContext.getShopId();
        LocalDateTime now = LocalDateTime.now();
        Coupon c = new Coupon();
        c.setShopId(shopId);
        c.setMemberId(dto.getMemberId());
        c.setName(dto.getName());
        c.setValue(dto.getValue());
        c.setThreshold(dto.getThreshold() == null ? 0 : dto.getThreshold());
        c.setStatus(0);
        c.setStartTime(now);
        c.setEndTime(now.plusDays(dto.getValidDays() == null ? 30 : dto.getValidDays()));
        c.setCreatedAt(now);
        couponMapper.insert(c);
        return R.ok(c.getId());
    }

    /** 作废优惠券：DELETE /api/admin/marketing/coupons/{id} */
    @DeleteMapping("/admin/marketing/coupons/{id}")
    public R<Void> revoke(@PathVariable Long id) {
        couponMapper.delete(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getId, id).eq(Coupon::getShopId, RequestContext.getShopId()));
        return R.ok();
    }
}
