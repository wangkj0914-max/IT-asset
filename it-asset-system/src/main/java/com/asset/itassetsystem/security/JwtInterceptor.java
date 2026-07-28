package com.asset.itassetsystem.security;

import com.asset.itassetsystem.service.SysGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证 + 用户组 ACL 权限拦截器
 *
 * GET 请求：大部分放行，敏感端点（用户列表、操作日志）需登录
 * POST/PUT/DELETE 请求（除 /login）：强制认证 + 用户组 ACL 权限检查
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    // 需要认证的敏感 GET 端点
    private static final String[] SENSITIVE_GET = {"/user/", "/log/", "/operation-log/", "/group/"};

    // 写操作路由 -> 所需权限
    private static final Map<String, String> WRITE_PERMISSIONS = new HashMap<>();
    static {
        WRITE_PERMISSIONS.put("/user/", "user.manage");
        WRITE_PERMISSIONS.put("/group/", "system.admin");
        WRITE_PERMISSIONS.put("/system-data/", "system.admin");
        WRITE_PERMISSIONS.put("/system-init/", "system.admin");
    }

    @Autowired
    private SysGroupService sysGroupService;

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

        // 将用户信息放入 request attribute（无论是否登录）
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

        // 非 GET 请求（写操作）需登录认证
        if (!"GET".equalsIgnoreCase(method) && !path.endsWith("/login")) {
            if (claims == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\"}");
                return false;
            }

            // 用户组 ACL 权限检查
            String requiredPerm = getRequiredPermission(path);
            if (requiredPerm != null) {
                Long userId = (Long) claims.get("userId");
                Integer role = (Integer) claims.get("role");
                if (!sysGroupService.hasPermission(userId, role, requiredPerm)) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"msg\":\"无权限：需要 " + requiredPerm + " 权限\"}");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 根据请求路径返回所需的权限标识，null 表示无需特殊权限
     */
    private String getRequiredPermission(String path) {
        for (Map.Entry<String, String> entry : WRITE_PERMISSIONS.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
