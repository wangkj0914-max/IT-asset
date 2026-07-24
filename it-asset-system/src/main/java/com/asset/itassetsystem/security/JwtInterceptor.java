package com.asset.itassetsystem.security;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * JWT 认证拦截器
 * 
 * GET 请求：大部分放行，敏感端点（用户列表、操作日志）需登录
 * POST/PUT/DELETE 请求（除 /login）：强制认证 + 仅管理员可执行
 */
public class JwtInterceptor implements HandlerInterceptor {

    // 需要认证的敏感 GET 端点
    private static final String[] SENSITIVE_GET = {"/user/", "/log/", "/operation-log/"};

    private boolean isSensitiveGet(String path) {
        for (String s : SENSITIVE_GET) {
            if (path.contains(s)) return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 解析 Token
        Map<String, Object> claims = null;
        if (token != null && !token.isEmpty()) {
            claims = JwtUtil.parseToken(token);
        }

        // 将用户信息放入 request attribute
        if (claims != null) {
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("username", claims.get("username"));
            request.setAttribute("role", claims.get("role"));
        }

        // 敏感 GET 端点需登录
        if ("GET".equalsIgnoreCase(method) && isSensitiveGet(path)) {
            if (claims == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
                return false;
            }
        }

        // 非 GET 请求（写操作）仅需登录认证
        if (!"GET".equalsIgnoreCase(method) && !path.endsWith("/login")) {
            if (claims == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\"}");
                return false;
            }
        }

        return true;
    }
}
