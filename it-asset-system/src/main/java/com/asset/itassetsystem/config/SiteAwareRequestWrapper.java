package com.asset.itassetsystem.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 站点感知的请求包装器：把 {@code site} 参数与 {@code X-Site} 请求头
 * 一律替换为 token 中的权威站点（大小写不敏感）。
 *
 * 覆盖面：`@RequestParam String site` 由 Spring 经
 * {@code ServletRequest#getParameterValues} 取值 → 命中；
 * {@code request.getParameter("site")} / {@code request.getHeader("X-Site")} 直读 → 命中。
 * 非 site 参数一律透传给被包装的原始请求。
 */
public class SiteAwareRequestWrapper extends HttpServletRequestWrapper {

    private static final String SITE_PARAM = "site";
    private static final String SITE_HEADER = "X-Site";

    /** 来自 token 的权威站点。 */
    private final String site;

    public SiteAwareRequestWrapper(HttpServletRequest request, String site) {
        super(request);
        this.site = site;
    }

    @Override
    public String getParameter(String name) {
        return SITE_PARAM.equalsIgnoreCase(name) ? site : super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return SITE_PARAM.equalsIgnoreCase(name) ? new String[]{ site } : super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        // Spring 某些绑定路径会读取整张 parameter map，必须一并覆盖
        Map<String, String[]> map = new LinkedHashMap<>(super.getParameterMap());
        map.put(SITE_PARAM, new String[]{ site });
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String getHeader(String name) {
        return SITE_HEADER.equalsIgnoreCase(name) ? site : super.getHeader(name);
    }
}
