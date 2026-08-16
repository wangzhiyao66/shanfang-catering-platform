package com.ordering.modules.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordering.modules.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
