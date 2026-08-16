package com.ordering.modules.member.service;

import com.ordering.modules.member.entity.Coupon;
import com.ordering.modules.member.entity.Member;

import java.math.BigDecimal;
import java.util.List;

public interface MemberService {

    /** 按 openid 取会员，不存在则注册（写入默认等级/积分0）。下单时调用。 */
    Member ensureMember(String openid, Long shopId);

    /** 会员档案 */
    Member getProfile(Long memberId);

    /** 绑定手机号 */
    void bindPhone(Long memberId, String phone);

    /** 变动积分：更新余额并写 points_log（change 正得负耗）。 */
    void addPoints(Long memberId, int change, String type, Long refId);

    /** 会员等级折扣（无等级返回 1.0） */
    BigDecimal discountOf(Long levelId);

    /** 会员优惠券列表（按 id 倒序；status 由前端区分可用/已用） */
    List<Coupon> listCoupons(Long shopId, Long memberId);
}
