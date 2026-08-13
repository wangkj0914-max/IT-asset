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
 * 除登录接口（/login）外，所有请求均要求登录认证。
 * 写操作（POST/PUT/DELETE）在登录基础上，还需通过用户组 ACL 权限检查。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    // 写操作路由 -> 所需权限
    private static final Map<String, String> WRITE_PERMISSIONS = new HashMap<>();
    static {
        // 资产管理
        WRITE_PERMISSIONS.put("/assetInfo/", "asset.edit");
        WRITE_PERMISSIONS.put("/category/", "asset.edit");
        WRITE_PERMISSIONS.put("/assetModel/", "asset.edit");
        WRITE_PERMISSIONS.put("/statusLabel/", "asset.edit");
        WRITE_PERMISSIONS.put("/storage-location/", "asset.edit");
        WRITE_PERMISSIONS.put("/department/", "asset.edit");
        WRITE_PERMISSIONS.put("/custom-field/", "asset.edit");
        // 资产操作
        WRITE_PERMISSIONS.put("/use/", "asset.own_use");
        WRITE_PERMISSIONS.put("/return/", "asset.own_use");
        WRITE_PERMISSIONS.put("/repair/", "asset.edit");
        WRITE_PERMISSIONS.put("/scrap/", "asset.edit");
        WRITE_PERMISSIONS.put("/transfer/", "asset.edit");
        WRITE_PERMISSIONS.put("/inventory/", "asset.edit");
        WRITE_PERMISSIONS.put("/accessory/", "asset.edit");
        WRITE_PERMISSIONS.put("/component/", "asset.edit");
        // 物资管理
        WRITE_PERMISSIONS.put("/consumable/", "consumable.manage");
        WRITE_PERMISSIONS.put("/license/", "license.manage");
        // 管理功能
        WRITE_PERMISSIONS.put("/user/", "user.manage");
        WRITE_PERMISSIONS.put("/group/", "system.admin");
        WRITE_PERMISSIONS.put("/system-data/", "system.admin");
        WRITE_PERMISSIONS.put("/system-init/", "system.admin");
        WRITE_PERMISSIONS.put("/workflow-config/", "system.admin");
        WRITE_PERMISSIONS.put("/notice/", "system.admin");
        WRITE_PERMISSIONS.put("/log/", "system.admin");
    }

    @Autowired
    private SysGroupService sysGroupService;

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

        // 登录接口放行（context-path 为 /asset，登录路径为 /asset/login）
        if (path.endsWith("/login")) {
            return true;
        }

        // 其余所有请求都要求登录认证
        if (claims == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            return false;
        }

        // 写操作（非 GET）在登录基础上，还需通过用户组 ACL 权限检查
        if (!"GET".equalsIgnoreCase(method)) {
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
