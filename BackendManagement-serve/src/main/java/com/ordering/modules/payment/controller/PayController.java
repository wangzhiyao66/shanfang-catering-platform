package com.ordering.modules.payment.controller;

import com.ordering.common.annotation.LoginRequired;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.payment.service.PayService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PayController {

    private final PayService payService;

    public PayController(PayService payService) {
        this.payService = payService;
    }

    /** 顾客端调起支付：POST /api/client/pay/prepay  { orderId } → JSAPI 支付参数 */
    @LoginRequired
    @PostMapping("/client/pay/prepay")
    public R<Map<String, String>> prepay(@RequestBody Map<String, Long> body) {
        Long orderId = body.get("orderId");
        return R.ok(payService.prepay(RequestContext.getShopId(), RequestContext.getOpenid(), orderId));
    }

    /**
     * 微信支付 v3 回调：POST /api/client/pay/notify
     * 微信服务器异步调用，不带 X-Shop-Id / 登录态（已在 WebMvcConfig 排除拦截）。
     * 成功返回 200 即视为已接收，否则微信会重试。
     */
    @PostMapping("/client/pay/notify")
    public R<Void> notify(@RequestBody String body,
                          @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
                          @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
                          @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
                          @RequestHeader(value = "Wechatpay-Serial", required = false) String serialNo) {
        payService.handleNotify(body, timestamp, nonce, signature, serialNo);
        return R.ok();
    }
}
