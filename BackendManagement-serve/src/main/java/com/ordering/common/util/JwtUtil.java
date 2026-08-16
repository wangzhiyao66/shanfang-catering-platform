package com.ordering.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具（jjwt 0.12.x）。后台登录签发，含 shopId / adminId 声明。
 */
public class JwtUtil {

    public static String generate(Long shopId, Long adminId, long ttlSeconds, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put("shopId", shopId);
        if (adminId != null) {
            claims.put("adminId", adminId);
        }
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000))
                .signWith(key)
                .compact();
    }

    public static Claims parse(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
