package org.example.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {

    private static final String SECRET = "PsychologicalCounseling2026SecretKey!@#$%";
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L; // 7天

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 JWT
     */
    public static String generate(Long userId, String username, Integer roleCode) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "username", username, "roleCode", roleCode))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 JWT Claims
     */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 token 是否有效
     */
    public static boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从 token 获取 userId
     */
    public static Long getUserId(String token) {
        return parse(token).get("userId", Long.class);
    }

    /**
     * 从 token 获取 username
     */
    public static String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    /**
     * 从 token 获取 roleCode
     */
    public static Integer getRoleCode(String token) {
        return parse(token).get("roleCode", Integer.class);
    }
}
