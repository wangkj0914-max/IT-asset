package com.asset.itassetsystem.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 站点上下文解析工具。
 *
 * <p>写操作落库时，若请求体 DTO 不含 site 字段、而落库表带 site 列（且该列无默认值），
 * 必须显式从当前请求解析站点并回填，否则在 STRICT_TRANS_TABLES 下会因缺列报错，
 * 或静默落 NULL 导致记录"消失"。
 */
public final class SiteContextUtil {

    private SiteContextUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 从当前请求上下文解析站点。
     *
     * <p>优先级：query 参数 {@code site} → request attribute {@code tokenSite}
     * （由 {@code SiteIsolationFilter} 写入）。两者都为空（null 或空串）或不在
     * Web 请求线程（如定时任务）时返回 {@code null}。
     *
     * @return 已 trim 的站点字符串；无法解析时返回 {@code null}
     */
    public static String resolveSite() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();

        String paramSite = request.getParameter("site");
        if (paramSite != null && !paramSite.trim().isEmpty()) {
            return paramSite.trim();
        }

        Object tokenSite = request.getAttribute("tokenSite");
        if (tokenSite != null && !tokenSite.toString().trim().isEmpty()) {
            return tokenSite.toString().trim();
        }

        return null;
    }
}
