package com.asset.itassetsystem.config;

import com.asset.itassetsystem.security.JwtUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 站点强制隔离过滤器（P0-2 核心）。
 *
 * 职责：
 * 1. 自行解析 token（Filter 早于 HandlerInterceptor，拿不到拦截器写的 attribute）；
 * 2. 把站点/角色写入 request attribute（tokenSite / tokenRole）；
 * 3. token 缺失或非法时直接放行，交由 {@link com.asset.itassetsystem.security.JwtInterceptor} 统一返回 401；
 * 4. 管理员（role=2）放行不覆盖，保留前端站点切换能力；
 * 5. 非管理员且 token 无站点时返回 401；
 * 6. 非管理员用 {@link SiteAwareRequestWrapper} 强制以 token 站点覆盖 param / header。
 *
 * 执行顺序：@Order(2)，排在 SecurityHeaderFilter（@Order(1)）之后；
 * Servlet Filter 天然先于 DispatcherServlet，因此先于 JwtInterceptor.preHandle 执行。
 */
@Component
@Order(2)
public class SiteIsolationFilter implements Filter {

    private static final String ADMIN_ROLE = "2";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        // 1) 自解析 token（Filter 早于 HandlerInterceptor，拿不到拦截器写的 attribute，必须自己解析）
        String token = request.getHeader("token");
        Map<String, Object> claims = (token == null || token.isEmpty())
                ? null : JwtUtil.parseToken(token);

        // 2) token 缺失/非法：直接放行，交给 JwtInterceptor 统一返回 401（不在此处越权处理）
        if (claims == null) {
            chain.doFilter(req, res);
            return;
        }

        // 3) 把站点与角色写入 attribute，供 Controller / RequestBodyAdvice 使用
        String site = (String) claims.get("site");
        Object role = claims.get("role");
        request.setAttribute("tokenSite", site);
        request.setAttribute("tokenRole", role);

        // 4) 管理员(role=2)：放行不覆盖 —— 保留站点切换能力
        if (role != null && ADMIN_ROLE.equals(String.valueOf(role))) {
            chain.doFilter(req, res);
            return;
        }

        // 5) 非管理员 + token 无站点：拒绝（防御性，正常用户必有 site）
        if (site == null || site.trim().isEmpty()) {
            HttpServletResponse resp = (HttpServletResponse) res;
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":401,\"msg\":\"站点未配置，请联系管理员\"}");
            return;
        }

        // 6) 非管理员：用 token 站点强制覆盖 param / header / body
        chain.doFilter(new SiteAwareRequestWrapper(request, site), res);
    }
}
