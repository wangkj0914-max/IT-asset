package com.asset.itassetsystem.aspect;

import com.asset.itassetsystem.entity.SysOperationLog;
import com.asset.itassetsystem.security.JwtUtil;
import com.asset.itassetsystem.service.SysOperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志 AOP 切面
 * 拦截所有 Controller POST 方法，记录操作日志
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private SysOperationLogService logService;

    /**
     * 拦截所有 POST 请求（排除登录接口）
     */
    @Around("(@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)) " +
            "&& !execution(* com.asset.itassetsystem.controller.LoginController.*(..))")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperationLog log = new SysOperationLog();
        log.setCreateTime(LocalDateTime.now());

        try {
            // 获取请求信息
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                log.setRequestUri(request.getRequestURI());
                log.setIp(getClientIp(request));
                log.setMethod(request.getMethod());

                // 从 JWT 解析用户信息
                String token = request.getHeader("token");
                if (token != null) {
                    Long userId = JwtUtil.getUserId(token);
                    String userName = JwtUtil.getUsername(token);
                    log.setUserId(userId);
                    log.setUserName(userName != null ? userName : "unknown");
                }
            }

            // 解析模块和操作
            String className = jp.getTarget().getClass().getSimpleName();
            String methodName = jp.getSignature().getName();
            log.setModule(extractModule(className));
            log.setOperation(extractOperation(className, methodName));

            // 请求参数（截断避免过长）
            Object[] args = jp.getArgs();
            if (args != null && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    if (arg instanceof HttpServletRequest) continue;
                    sb.append(safeToString(arg));
                    if (sb.length() > 500) { sb.setLength(500); sb.append("..."); break; }
                }
                log.setRequestParams(sb.toString());
            }

            // 执行方法
            Object result = jp.proceed();

            log.setStatus(1);
            log.setCostTime(System.currentTimeMillis() - start);
            logService.save(log);
            return result;

        } catch (Throwable e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "unknown");
            log.setCostTime(System.currentTimeMillis() - start);
            logService.save(log);
            throw e;
        }
    }

    private String extractModule(String className) {
        if (className.contains("Asset")) return "资产管理";
        if (className.contains("Repair")) return "资产维修";
        if (className.contains("Scrap")) return "资产报废";
        if (className.contains("Inventory")) return "资产盘点";
        if (className.contains("Use")) return "资产领用";
        if (className.contains("Return")) return "资产归还";
        if (className.contains("Transfer")) return "资产调拨";
        if (className.contains("Inbound")) return "资产入库";
        if (className.contains("Category")) return "资产分类";
        if (className.contains("Department")) return "部门管理";
        if (className.contains("User")) return "用户管理";
        if (className.contains("SystemData")) return "系统管理";
        if (className.contains("SystemInit")) return "系统管理";
        return className;
    }

    private String extractOperation(String className, String methodName) {
        String m = methodName.toLowerCase();
        if (m.contains("apply") || m.contains("create") || m.contains("save") || m.contains("add")) return "ADD";
        if (m.contains("update") || m.contains("edit") || m.contains("modify")) return "UPDATE";
        if (m.contains("delete") || m.contains("remove")) return "DELETE";
        if (m.contains("approve") || m.contains("audit")) return "APPROVE";
        if (m.contains("complete") || m.contains("finish")) return "COMPLETE";
        if (m.contains("reset")) return "RESET";
        return "UPDATE";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        return ip != null ? ip : "0.0.0.0";
    }

    private String safeToString(Object obj) {
        if (obj == null) return "";
        try {
            String s = obj.toString();
            return s.length() > 300 ? s.substring(0, 300) + "..." : s;
        } catch (Exception e) { return "[无法序列化]"; }
    }
}
