package com.ordering.modules.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员：微信 openid 为登录凭证；points 积分余额、balance 储值余额（分）。
 */
@Data
@TableName("`member`")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String openid;
    private String unionid;
    private String phone;
    private String nickname;
    private String avatar;
    private Long levelId;
    private Integer points;
    private Integer balance;   // 储值余额（分）
    private Integer isBlocked;
    private LocalDateTime lastActiveAt;
}
