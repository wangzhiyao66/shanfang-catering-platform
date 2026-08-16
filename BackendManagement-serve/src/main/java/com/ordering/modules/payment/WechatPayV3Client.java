package com.ordering.modules.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordering.common.config.WechatProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付 v3（JSAPI）客户端：用 JDK 内置 RSA / AES 实现请求签名与回调解密，不引入额外 SDK。
 * 真实联调需配置 ordering.wechat：appid / mch-id / api-v3-key / private-key / merchant-serial-no / platform-cert。
 */
@Component
public class WechatPayV3Client {

    private static final String BASE = "https://api.mch.weixin.qq.com";
    private final WechatProperties wechat;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    public WechatPayV3Client(WechatProperties wechat) {
        this.wechat = wechat;
    }

    /**
     * JSAPI 下单，返回给小程序拉起支付的参数（appId/timeStamp/nonceStr/package/signType/paySign）。
     * 未配置真实商户私钥时抛出清晰的异常，便于联调定位。
     */
    public Map<String, String> jsapiPrepay(Long shopId, String openid, String outTradeNo, int amountFen, String description) {
        if (wechat.getPrivateKey() == null || wechat.getPrivateKey().isBlank()
                || wechat.getMerchantSerialNo() == null || wechat.getMerchantSerialNo().isBlank()) {
            throw new IllegalStateException("微信支付 v3 未配置商户私钥/序列号（ordering.wechat.private-key、merchant-serial-no）");
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("mchid", wechat.getMchId());
            body.put("appid", wechat.getAppid());
            body.put("description", description);
            body.put("out_trade_no", outTradeNo);
            body.put("notify_url", wechat.getNotifyUrl());
            Map<String, Object> amount = new HashMap<>();
            amount.put("total", amountFen);
            amount.put("currency", "CNY");
            body.put("amount", amount);
            Map<String, Object> payer = new HashMap<>();
            payer.put("openid", openid);
            body.put("payer", payer);

            String url = BASE + "/v3/pay/transactions/jsapi";
            String resp = postWithAuth(url, objectMapper.writeValueAsString(body));
            JsonNode node = objectMapper.readTree(resp);
            String prepayId = node.get("prepay_id").asText();

            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String pkg = "prepay_id=" + prepayId;
            String message = wechat.getAppid() + "\n" + timeStamp + "\n" + nonceStr + "\n" + pkg + "\n";
            String paySign = sign(message);

            Map<String, String> r = new HashMap<>();
            r.put("appId", wechat.getAppid());
            r.put("timeStamp", timeStamp);
            r.put("nonceStr", nonceStr);
            r.put("package", pkg);
            r.put("signType", "RSA");
            r.put("paySign", paySign);
            return r;
        } catch (Exception e) {
            throw new RuntimeException("微信支付下单失败：" + e.getMessage(), e);
        }
    }

    /** 解密回调中的 resource（AES-256-GCM，密钥为 api-v3-key），返回明文 JSON 字符串。 */
    public String decryptResource(JsonNode resource) {
        try {
            String ciphertext = resource.get("ciphertext").asText();
            String nonce = resource.get("nonce").asText();
            String associated = resource.has("associated_data") && !resource.get("associated_data").isNull()
                    ? resource.get("associated_data").asText() : "";
            byte[] key = ApiV3Key.decode(wechat.getApiV3Key());
            byte[] data = Base64.getDecoder().decode(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcm = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcm);
            if (!associated.isEmpty()) {
                cipher.updateAAD(associated.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plain = cipher.doFinal(data);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("微信支付回调解密失败：" + e.getMessage(), e);
        }
    }

    /** 校验回调签名（用平台证书公钥）。 */
    public boolean verifyNotify(String timestamp, String nonce, String body, String signature, String serialNo) {
        try {
            if (wechat.getPlatformCert() == null || wechat.getPlatformCert().isBlank()) {
                return false; // 未配置平台证书时跳过验签（生产必须配置）
            }
            PublicKey pub = loadPublicKey(wechat.getPlatformCert());
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pub);
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    // ===== 内部 =====

    private String postWithAuth(String url, String jsonBody) {
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = sign(buildSignMessage("POST", url.replace(BASE, ""), timestamp, nonceStr, jsonBody));
        String auth = "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + wechat.getMchId() + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "signature=\"" + signature + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + wechat.getMerchantSerialNo() + "\"";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", auth);
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(jsonBody, headers);
        return rest.postForObject(URI.create(url), entity, String.class);
    }

    private String buildSignMessage(String method, String urlPath, String timestamp, String nonceStr, String body) {
        return method + "\n" + urlPath + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
    }

    private String sign(String message) {
        try {
            PrivateKey key = loadPrivateKey(wechat.getPrivateKey());
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(key);
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new RuntimeException("微信支付签名失败：" + e.getMessage(), e);
        }
    }

    private static PrivateKey loadPrivateKey(String pem) {
        try {
            byte[] der = pemBytes(pem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new RuntimeException("加载商户私钥失败：" + e.getMessage(), e);
        }
    }

    private static PublicKey loadPublicKey(String pem) {
        try {
            byte[] der = pemBytes(pem);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new RuntimeException("加载平台证书失败：" + e.getMessage(), e);
        }
    }

    private static byte[] pemBytes(String pem) {
        String clean = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(clean);
    }

    /** api-v3-key 解码：优先 Base64，失败则按十六进制。 */
    private static class ApiV3Key {
        static byte[] decode(String key) {
            try {
                return Base64.getDecoder().decode(key);
            } catch (Exception e) {
                return hexToBytes(key);
            }
        }

        private static byte[] hexToBytes(String s) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int i = 0; i < s.length(); i += 2) {
                out.write(Integer.parseInt(s.substring(i, i + 2), 16));
            }
            return out.toByteArray();
        }
    }
}
