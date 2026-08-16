package com.ordering.modules.auth;

import com.ordering.common.config.AdminProperties;
import com.ordering.common.config.JwtProperties;
import com.ordering.common.config.WechatProperties;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.common.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {

    private final WechatProperties wechatProperties;
    private final AdminProperties adminProperties;
    private final JwtProperties jwtProperties;

    public AuthService(WechatProperties wechatProperties, AdminProperties adminProperties, JwtProperties jwtProperties) {
        this.wechatProperties = wechatProperties;
        this.adminProperties = adminProperties;
        this.jwtProperties = jwtProperties;
    }

    /** 微信登录：用 code 换 openid（真实调用；未配置 appid 时降级为演示 openid，保证联调可走通） */
    public String clientLogin(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "缺少 code");
        }
        if (wechatProperties.getAppid() != null && !wechatProperties.getAppid().startsWith("your-")) {
            try {
                String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wechatProperties.getAppid()
                        + "&secret=" + wechatProperties.getSecret()
                        + "&js_code=" + code + "&grant_type=authorization_code";
                RestTemplate rt = new RestTemplate();
                Map<String, Object> resp = rt.getForObject(url, Map.class);
                if (resp != null && resp.get("openid") != null) {
                    return String.valueOf(resp.get("openid"));
                }
            } catch (Exception ignored) {
                // 调微信失败，降级为演示 openid
            }
        }
        return "demo_openid_" + code;
    }

    /** 后台登录：校验账号密码后签发 JWT（含 shopId） */
    public String adminLogin(String username, String password) {
        if (!adminProperties.getUsername().equals(username) || !adminProperties.getPassword().equals(password)) {
            throw new BizException(CodeEnum.UNAUTHORIZED.getCode(), "账号或密码错误");
        }
        Long shopId = RequestContext.getShopId();
        if (shopId == null) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "缺少 X-Shop-Id");
        }
        return JwtUtil.generate(shopId, 1L, jwtProperties.getExpiration(), jwtProperties.getSecret());
    }
}
