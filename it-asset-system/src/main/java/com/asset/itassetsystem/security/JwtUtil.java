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
 *
 * 安全说明（P0）：
 * - 密钥不再有任何硬编码默认值，必须由 {@link com.asset.itassetsystem.config.JwtConfig}
 *   在启动期经 {@link #setSecret(String)} 注入；缺失/过短一律 fail-fast。
 * - payload 中的 username / site 经 JSON 转义，防止引号/反斜杠破坏 JSON 结构。
 */
public class JwtUtil {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000L; // 8 小时

    /** JWT 密钥（无初值）：必须由 JwtConfig 注入，杜绝弱默认值。 */
    private static volatile String secret;

    private JwtUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 设置密钥（从配置注入），并在注入时做防御性校验。
     *
     * @param secret 密钥明文，长度必须 ≥ 32
     * @throws IllegalStateException 密钥为 null/空或长度不足时抛出，触发容器启动失败
     */
    public static void setSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT 密钥未配置（JWT_SECRET）");
        }
        if (secret.trim().length() < 32) {
            throw new IllegalStateException("JWT 密钥长度不足 32 位，拒绝启动");
        }
        JwtUtil.secret = secret.trim();
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（1=普通用户, 2=管理员）
     * @param site     所属站点（可为 null/空，将写入空串）
     * @return JWT token 字符串
     */
    public static String generateToken(Long userId, String username, Integer role, String site) {
        long now = System.currentTimeMillis();
        long exp = now + EXPIRATION_MS;

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        // 注意：site 必须追加在 exp 之后（extractLong 以 ',' / '}' 为界，放前面会破坏 exp 边界）；
        // username / site 均经 jsonEscape，防止引号/反斜杠破坏 JSON 结构。
        String payloadJson = String.format(
            "{\"userId\":%d,\"username\":\"%s\",\"role\":%d,\"iat\":%d,\"exp\":%d,\"site\":\"%s\"}",
            userId, jsonEscape(username), role, now / 1000, exp / 1000,
            jsonEscape(site == null ? "" : site)
        );

        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }

    /**
     * 验证并解析 Token
     *
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
            // 站点声明（先 unescape 还原 jsonEscape 的转义）
            claims.put("site", jsonUnescape(extractString(payloadJson, "site")));

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
        // 注意：parseToken 将 role 存为 Integer，此处用 Number 取值以避免 Integer→Long 的 ClassCastException
        return claims != null ? ((Number) claims.get("role")).intValue() : null;
    }

    /**
     * 从 Token 中提取站点（便捷方法）
     */
    public static String getSite(String token) {
        Map<String, Object> claims = parseToken(token);
        return claims != null ? (String) claims.get("site") : null;
    }

    // ====== 私有方法 ======

    private static String sign(String data) {
        // 保险丝：密钥未初始化时拒绝签名，避免用空密钥生成可被伪造的 token
        if (secret == null) {
            throw new IllegalStateException("JWT 密钥未初始化");
        }
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

    /**
     * JSON 字符串转义（\、"、换行、回车、制表符）。
     */
    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * JSON 字符串反转义（与 {@link #jsonEscape(String)} 严格互逆）。
     *
     * 必须单遍扫描：顺序 replace 会把「字面反斜杠 + 字母 n」（转义后为 {@code \\n}）
     * 误配成换行符，导致不可逆。此处遇到 {@code \} 时读取下一个字符再决定还原结果。
     */
    private static String jsonUnescape(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '\\':
                        sb.append('\\');
                        i++;
                        break;
                    case '"':
                        sb.append('"');
                        i++;
                        break;
                    case 'n':
                        sb.append('\n');
                        i++;
                        break;
                    case 'r':
                        sb.append('\r');
                        i++;
                        break;
                    case 't':
                        sb.append('\t');
                        i++;
                        break;
                    default:
                        // 未知转义序列：保留反斜杠本身，下一字符按普通字符继续处理
                        sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
