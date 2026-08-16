package com.ordering.modules.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.kitchen.service.KitchenPushService;
import com.ordering.modules.member.entity.Member;
import com.ordering.modules.member.service.MemberService;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.entity.OrderPayment;
import com.ordering.modules.order.mapper.OrderMapper;
import com.ordering.modules.order.mapper.OrderPaymentMapper;
import com.ordering.modules.payment.WechatPayV3Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final MemberService memberService;
    private final WechatPayV3Client wechatPayV3Client;
    private final KitchenPushService kitchenPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayServiceImpl(OrderMapper orderMapper, OrderPaymentMapper orderPaymentMapper,
                          MemberService memberService, WechatPayV3Client wechatPayV3Client,
                          KitchenPushService kitchenPushService) {
        this.orderMapper = orderMapper;
        this.orderPaymentMapper = orderPaymentMapper;
        this.memberService = memberService;
        this.wechatPayV3Client = wechatPayV3Client;
        this.kitchenPushService = kitchenPushService;
    }

    @Override
    public Map<String, String> prepay(Long shopId, String openid, Long orderId) {
        Member m = memberService.ensureMember(openid, shopId);
        Order o = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getShopId, shopId).eq(Order::getMemberId, m.getId()));
        if (o == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单不存在");
        }
        if (o.getStatus() != 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "订单状态不可支付");
        }
        // 幂等：pay_no 复用 order_no；已存在支付记录则直接复用
        OrderPayment p = orderPaymentMapper.selectOne(
                new LambdaQueryWrapper<OrderPayment>().eq(OrderPayment::getPayNo, o.getOrderNo()));
        if (p == null) {
            p = new OrderPayment();
            p.setOrderId(o.getId());
            p.setPayNo(o.getOrderNo());
            p.setChannel("wechat");
            p.setAmount(o.getPayAmount());
            p.setStatus(0);
            orderPaymentMapper.insert(p);
        }
        return wechatPayV3Client.jsapiPrepay(shopId, openid, o.getOrderNo(), o.getPayAmount(), "膳房点餐");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(String body, String timestamp, String nonce, String signature, String serialNo) {
        try {
            // 1) 验签（配置了平台证书才验）
            if (!wechatPayV3Client.verifyNotify(timestamp, nonce, body, signature, serialNo)) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "回调签名校验失败");
            }
            // 2) 解密 resource
            JsonNode root = objectMapper.readTree(body);
            String plain = wechatPayV3Client.decryptResource(root.get("resource"));
            JsonNode resp = objectMapper.readTree(plain);
            String outTradeNo = resp.get("out_trade_no").asText();
            String tradeState = resp.has("trade_state") ? resp.get("trade_state").asText() : "SUCCESS";

            OrderPayment p = orderPaymentMapper.selectOne(
                    new LambdaQueryWrapper<OrderPayment>().eq(OrderPayment::getPayNo, outTradeNo));
            if (p == null) {
                throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "支付记录不存在");
            }
            // 3) 幂等：已成功直接返回
            if (p.getStatus() == 1) {
                return;
            }
            if (!"SUCCESS".equals(tradeState)) {
                p.setStatus(2); // 失败
                orderPaymentMapper.updateById(p);
                return;
            }
            // 4) 标记支付成功
            p.setStatus(1);
            p.setPaidAt(LocalDateTime.now());
            orderPaymentMapper.updateById(p);

            // 5) 推进订单 → 已支付/待接单(1)
            Order o = orderMapper.selectById(p.getOrderId());
            if (o != null && o.getStatus() == 0) {
                Order upd = new Order();
                upd.setId(o.getId());
                upd.setStatus(1);
                upd.setPaidAt(LocalDateTime.now());
                upd.setVersion(o.getVersion());
                orderMapper.updateById(upd);
                // 6) 记积分（1 元 = 1 分）
                int points = o.getPayAmount() == null ? 0 : o.getPayAmount() / 100;
                if (points > 0 && o.getMemberId() != null) {
                    memberService.addPoints(o.getMemberId(), points, "earn", o.getId());
                }
                Map<String, Object> payload = new HashMap<>();
                payload.put("type", "paid");
                payload.put("orderId", o.getId());
                payload.put("status", 1);
                kitchenPushService.broadcast(o.getShopId(), payload);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "回调处理失败：" + e.getMessage());
        }
    }
}
