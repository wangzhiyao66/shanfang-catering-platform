package com.ordering.modules.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.member.entity.Coupon;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.entity.MemberLevel;
import com.ordering.modules.member.entity.PointsLog;
import com.ordering.modules.member.mapper.CouponMapper;
import com.ordering.modules.member.mapper.MemberLevelMapper;
import com.ordering.modules.member.mapper.MemberMapper;
import com.ordering.modules.member.mapper.PointsLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final PointsLogMapper pointsLogMapper;
    private final CouponMapper couponMapper;

    public MemberServiceImpl(MemberMapper memberMapper, MemberLevelMapper memberLevelMapper, PointsLogMapper pointsLogMapper, CouponMapper couponMapper) {
        this.memberMapper = memberMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.pointsLogMapper = pointsLogMapper;
        this.couponMapper = couponMapper;
    }

    @Override
    public Member ensureMember(String openid, Long shopId) {
        Member m = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getOpenid, openid).eq(Member::getShopId, shopId));
        if (m == null) {
            m = new Member();
            m.setShopId(shopId);
            m.setOpenid(openid);
            m.setPoints(0);
            m.setBalance(0);
            m.setIsBlocked(0);
            m.setLevelId(1L); // 默认普通会员（种子数据 id=1）
            memberMapper.insert(m);
        }
        return m;
    }

    @Override
    public Member getProfile(Long memberId) {
        Member m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "会员不存在");
        }
        return m;
    }

    @Override
    public void bindPhone(Long memberId, String phone) {
        Member m = getProfile(memberId);
        m.setPhone(phone);
        m.setLastActiveAt(LocalDateTime.now());
        memberMapper.updateById(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long memberId, int change, String type, Long refId) {
        Member m = getProfile(memberId);
        int base = m.getPoints() == null ? 0 : m.getPoints();
        m.setPoints(Math.max(0, base + change));
        memberMapper.updateById(m);

        PointsLog log = new PointsLog();
        log.setMemberId(memberId);
        log.setChange(change);
        log.setType(type);
        log.setRefId(refId);
        pointsLogMapper.insert(log);
    }

    @Override
    public BigDecimal discountOf(Long levelId) {
        if (levelId == null) {
            return BigDecimal.ONE;
        }
        MemberLevel lvl = memberLevelMapper.selectById(levelId);
        return lvl == null || lvl.getDiscount() == null ? BigDecimal.ONE : lvl.getDiscount();
    }

    @Override
    public List<Coupon> listCoupons(Long shopId, Long memberId) {
        List<Coupon> list = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .eq(Coupon::getMemberId, memberId)
                .orderByDesc(Coupon::getId));
        // 回填展示用到期日 yyyy-MM-dd
        list.forEach(c -> {
            if (c.getEndTime() != null) {
                c.setValidTo(c.getEndTime().toLocalDate().toString());
            }
        });
        return list;
    }
}
