package com.asset.itassetsystem.common;

import com.asset.itassetsystem.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用工具类
 */
public class CommonUtil {

    /**
     * 构建分页响应数据
     */
    public static Map<String, Object> buildPageResult(com.baomidou.mybatisplus.core.metadata.IPage<?> page) {
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return data;
    }

    /**
     * 从 JWT Token 解析用户 ID
     */
    public static Long parseUserIdFromToken(String token) {
        return JwtUtil.getUserId(token);
    }

    /**
     * 从 JWT Token 解析用户名
     */
    public static String parseUsernameFromToken(String token) {
        String username = JwtUtil.getUsername(token);
        return username != null ? username : "anonymous";
    }

    /**
     * 从 JWT Token 解析用户角色
     */
    public static Integer parseRoleFromToken(String token) {
        return JwtUtil.getRole(token);
    }
}
