package com.asset.itassetsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * 闭合 {@code @RequestBody} JSON 站点盲区的第二道防线。
 *
 * 请求体不走 {@code getParameter}，因此 {@link SiteAwareRequestWrapper} 覆盖不到；
 * 本 Advice 在 body 反序列化完成后，按下述策略处理 body 中的 site 字段。
 *
 * <p><b>非管理员（role != 2）</b>：token 站点权威 —— 用 token 站点强制覆盖 body.site，
 * 保证站点隔离目标（P0-2）不被前端伪造站点绕过。
 *
 * <p><b>管理员（role == 2）</b>：保留其"切换站点"能力，同时杜绝 NULL/静默落站点：
 * <ol>
 *   <li>body.site 已有非空值 → 原样保留（尊重管理员的显式选择）；</li>
 *   <li>body.site 为空/null → 用 {@code request.getParameter("site")} 兜底填入
 *       （前端 request.js 每个请求都会带上当前站点，即管理员"看到的站点"）；</li>
 *   <li>该参数也为空 → 退回 {@code request.getAttribute("tokenSite")}（管理员自身站点）；</li>
 *   <li>仍为空 → 保持原样并记一条 warn 日志，不抛异常打断请求。</li>
 * </ol>
 * 这一步是「去掉 DB 列 site 默认值（fix_site_default.sql）」的安全前提：否则管理员
 * 不传 site 的写操作会从"静默落苏州"变成"静默落 NULL"，记录从所有站点列表消失。
 *
 * 显式分派三种 body 形态（历史实现仅反射 setSite，会静默漏掉集合类型）：
 * - {@link Collection}：遍历元素逐个处理（如 AssetInfoController 的 batchSave 接收 List&lt;AssetInfo&gt;）；
 * - {@link Map}：处理 {@code site} 键；
 * - 普通对象：尝试 getSite/setSite。
 */
@ControllerAdvice
public class SiteOverrideRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(SiteOverrideRequestBodyAdvice.class);

    private static final String ADMIN_ROLE = "2";

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 对所有 @RequestBody 生效（真正的判断在 afterBodyRead 内）
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        if (body == null) {
            return null;
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return body;
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();

        Object role = request.getAttribute("tokenRole");
        boolean isAdmin = role != null && ADMIN_ROLE.equals(String.valueOf(role));

        if (isAdmin) {
            // 管理员：不用 token 站点覆盖其显式选择，仅在缺失时兜底填充
            String fallback = resolveFallbackSite(request);
            if (fallback == null) {
                log.warn("管理员写请求未携带站点，且无 query site / tokenSite 可兜底，body.site 保持原样: {}",
                        body.getClass().getName());
                return body;
            }
            fillSiteIfEmpty(body, fallback);
            return body;
        }

        // 非管理员：token 站点权威，强制覆盖
        String site = (String) request.getAttribute("tokenSite");
        if (site == null || site.isEmpty()) {
            return body;
        }
        overrideSite(body, site);
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * 管理员的兜底站点来源：优先前端当前站点（query 参数），其次 token 站点。
     *
     * @return 兜底站点（已 trim）；均无则返回 {@code null}
     */
    private String resolveFallbackSite(HttpServletRequest request) {
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

    /**
     * 按 body 实际形态分派"仅在缺失时填充"逻辑（管理员路径：不覆盖已有显式值）。
     *
     * <p>三种形态的填充语义刻意不同：
     * <ul>
     *   <li><b>普通对象 / Collection</b>：实体只要暴露 {@code getSite()/setSite(String)}
     *       就应该有站点，因此 site 为空时一律填上，杜绝 NULL；</li>
     *   <li><b>Map</b>：<b>仅当 {@code site} 键已存在且其值为空时</b>才填充。
     *       键缺失不注入 —— Map 语义下"无该键"可能表示"该 Map 与站点无关"，
     *       凭空注入 {@code site} 会形成侵入式污染。此处为防御性代码，宁可少改。
     *       <b>请勿改为无条件注入。</b></li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private void fillSiteIfEmpty(Object body, String site) {
        if (body instanceof Collection) {
            for (Object element : (Collection<Object>) body) {
                if (element != null) {
                    setSiteIfEmpty(element, site);
                }
            }
        } else if (body instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) body;
            // 仅当 site 键已存在且其值为空时才填充；键缺失则原样返回，不凭空注入
            if (map.containsKey("site")) {
                Object current = map.get("site");
                if (current == null || current.toString().trim().isEmpty()) {
                    map.put("site", site);
                }
            }
        } else {
            setSiteIfEmpty(body, site);
        }
    }

    /**
     * 仅当目标暴露 {@code getSite()}/{@code setSite(String)} 且当前 site 为空时填入；
     * 无 site 字段的 DTO 静默跳过。
     */
    private void setSiteIfEmpty(Object target, String site) {
        try {
            Method getter = target.getClass().getMethod("getSite");
            Object current = getter.invoke(target);
            if (current != null && !current.toString().trim().isEmpty()) {
                // 已有显式站点，尊重之
                return;
            }
            Method setter = target.getClass().getMethod("setSite", String.class);
            setter.invoke(target, site);
        } catch (NoSuchMethodException ignore) {
            // 无 site 字段的 DTO，跳过
        } catch (Exception e) {
            throw new IllegalStateException("站点兜底失败: " + target.getClass().getName(), e);
        }
    }

    /**
     * 按 body 实际形态分派覆盖逻辑（非管理员路径：无条件覆盖为 token 站点）。
     */
    @SuppressWarnings("unchecked")
    private void overrideSite(Object body, String site) {
        if (body instanceof Collection) {
            // 漏洞 A 修复：集合类型（如 List<AssetInfo>）逐元素覆盖，避免反射 setSite 直接命中不到
            for (Object element : (Collection<Object>) body) {
                if (element != null) {
                    setSiteIfPresent(element, site);
                }
            }
        } else if (body instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) body;
            if (map.containsKey("site")) {
                map.put("site", site);
            }
        } else {
            setSiteIfPresent(body, site);
        }
    }

    /**
     * 仅当目标暴露 {@code setSite(String)} 时覆盖，无该字段的 DTO 静默跳过。
     */
    private void setSiteIfPresent(Object target, String site) {
        try {
            Method setter = target.getClass().getMethod("setSite", String.class);
            setter.invoke(target, site);
        } catch (NoSuchMethodException ignore) {
            // 无 site 字段的 DTO，跳过
        } catch (Exception e) {
            throw new IllegalStateException("站点强制失败: " + target.getClass().getName(), e);
        }
    }
}
