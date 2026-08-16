package com.ordering.modules.payment.service;

import java.util.Map;

public interface PayService {

    /** 顾客端下单后调起支付：返回小程序 JSAPI 支付参数 */
    Map<String, String> prepay(Long shopId, String openid, Long orderId);

    /** 微信支付回调：验签 + 解密 + 幂等更新订单/支付状态，并记积分 */
    void handleNotify(String body, String timestamp, String nonce, String signature, String serialNo);
}
