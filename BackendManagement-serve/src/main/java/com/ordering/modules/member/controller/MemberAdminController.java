package com.ordering.modules.member.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.entity.MemberLevel;
import com.ordering.modules.member.mapper.MemberLevelMapper;
import com.ordering.modules.member.mapper.MemberMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MemberAdminController {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;

    public MemberAdminController(MemberMapper memberMapper, MemberLevelMapper memberLevelMapper) {
        this.memberMapper = memberMapper;
        this.memberLevelMapper = memberLevelMapper;
    }

    /** 后台会员列表：GET /api/admin/members */
    @GetMapping("/admin/members")
    public R<List<Member>> list() {
        return R.ok(memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .eq(Member::getShopId, RequestContext.getShopId())
                .orderByDesc(Member::getId)));
    }

    /** 后台会员等级：GET /api/admin/member/levels */
    @GetMapping("/admin/member/levels")
    public R<List<MemberLevel>> levels() {
        return R.ok(memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                .eq(MemberLevel::getShopId, RequestContext.getShopId())));
    }
}
