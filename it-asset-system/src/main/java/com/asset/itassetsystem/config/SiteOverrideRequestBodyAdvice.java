package com.asset.itassetsystem.config;

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
 * 本 Advice 在 body 反序列化完成后，用 token 站点覆盖 body 中的 site 字段。
 *
 * 显式分派三种 body 形态（历史实现仅反射 setSite，会静默漏掉集合类型）：
 * - {@link Collection}：遍历元素逐个尝试 setSite（如 AssetInfoController 的 batchSave 接收 List&lt;AssetInfo&gt;）；
 * - {@link Map}：若含 {@code site} 键则覆盖；
 * - 普通对象：尝试 setSite。
 *
 * 安全约束：管理员（role=2）首行直接返回 body，保证切换站点时 body 内的站点不被改写。
 */
@ControllerAdvice
public class SiteOverrideRequestBodyAdvice implements RequestBodyAdvice {

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

        // 管理员（role=2）：跳过，保证切换站点时 body 内的站点不被改写
        Object role = request.getAttribute("tokenRole");
        if (role != null && ADMIN_ROLE.equals(String.valueOf(role))) {
            return body;
        }

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
     * 按 body 实际形态分派覆盖逻辑。
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
