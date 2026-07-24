package com.asset.itassetsystem.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（HMAC-SHA256 签名，零外部依赖）
 * 
 * Token 格式：base64(header).base64(payload).base64(signature)
 * 前端通过 HTTP header "token" 传递
 */
public class JwtUtil {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000L; // 8 小时
    private static String secret = "it-asset-system-default-secret-key-2026";

    /**
     * 设置密钥（从配置注入）
     */
    public static void setSecret(String secret) {
        JwtUtil.secret = secret;
    }

    /**
     * 生成 JWT Token
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（1=普通用户, 2=管理员）
     * @return JWT token 字符串
     */
    public static String generateToken(Long userId, String username, Integer role) {
        long now = System.currentTimeMillis();
        long exp = now + EXPIRATION_MS;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format(
            "{\"userId\":%d,\"username\":\"%s\",\"role\":%d,\"iat\":%d,\"exp\":%d}",
            userId, username, role, now / 1000, exp / 1000
        );

        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }

    /**
     * 验证并解析 Token
     * @param token JWT token
     * @return payload Map，验证失败返回 null
     */
    public static Map<String, Object> parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String headerB64 = parts[0];
            String payloadB64 = parts[1];
            String signature = parts[2];

            // 验签
            String signingInput = headerB64 + "." + payloadB64;
            String expectedSig = sign(signingInput);
            if (!signature.equals(expectedSig)) {
                return null;
            }

            // 解析 payload
            String payloadJson = new String(base64UrlDecode(payloadB64), StandardCharsets.UTF_8);
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", extractLong(payloadJson, "userId"));
            claims.put("username", extractString(payloadJson, "username"));
            claims.put("role", (int) extractLong(payloadJson, "role"));
            claims.put("exp", extractLong(payloadJson, "exp"));

            // 检查过期
            long exp = (long) claims.get("exp");
            if (System.currentTimeMillis() / 1000 > exp) {
                return null;
            }

            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中提取 userId
     */
    public static Long getUserId(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? (Long) claims.get("userId") : null;
    }

    /**
     * 从 Token 中提取 username
     */
    public static String getUsername(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从 Token 中提取 role
     */
    public static Integer getRole(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? ((Long) claims.get("role")).intValue() : null;
    }

    // ====== 私有方法 ======

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG);
            mac.init(keySpec);
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(sig);
        } catch (Exception e) {
            throw new RuntimeException("HMAC签名失败", e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private static long extractLong(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf(',', start);
        if (end == -1) end = json.indexOf('}', start);
        return Long.parseLong(json.substring(start, end).trim());
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
