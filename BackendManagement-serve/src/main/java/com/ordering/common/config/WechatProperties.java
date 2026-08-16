package com.ordering.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ordering.wechat")
public class WechatProperties {
    private String appid;
    private String secret;
    private String mchId;
    private String apiV3Key;
    private String notifyUrl;
    /** 商户 API 私钥（apiclient_key.pem 内容，PEM 文本，用于 v3 请求签名） */
    private String privateKey;
    /** 商户证书序列号（APIv3 证书管理中查看） */
    private String merchantSerialNo;
    /** 微信支付平台证书（PEM 文本，用于验签回调）；为空时由 /v3/certificates 自动拉取 */
    private String platformCert;
}
