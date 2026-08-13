package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 系统数据清理控制器（仅管理员可操作）
 */
@RestController
@RequestMapping("/system-data")
public class SystemDataController {

    @Autowired
    private DataSource dataSource;

    /**
     * 验证管理员权限
     */
    private boolean checkAdminPermission(String token) {
        if (token == null || token.isEmpty()) return false;
        Integer role = JwtUtil.getRole(token);
        return role != null && role == 2;
    }

    /**
     * 清空领用记录
     */
    @PostMapping("/clear-use-records")
    public Result<String> clearUseRecords(@RequestHeader("token") String token) {
        if (!checkAdminPermission(token)) {
            return Result.fail("无权限：仅管理员可执行此操作");
        }
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("TRUNCATE TABLE asset_use_record");
            stmt.execute("UPDATE asset_info SET status = 0, user_id = NULL");
            stmt.close();
            conn.close();
            return Result.success("领用记录已清空，资产状态已重置");
        } catch (Exception e) {
            return Result.error("清空失败：" + e.getMessage());
        }
    }

    /**
     * 清空入库记录
     */
    @PostMapping("/clear-inbound-records")
    public Result<String> clearInboundRecords(@RequestHeader("token") String token) {
        if (!checkAdminPermission(token)) {
            return Result.fail("无权限：仅管理员可执行此操作");
        }
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("TRUNCATE TABLE asset_inbound");
            stmt.close();
            conn.close();
            return Result.success("入库记录已清空");
        } catch (Exception e) {
            return Result.error("清空失败：" + e.getMessage());
        }
    }

    /**
     * 清空所有数据（谨慎使用）
     */
    @PostMapping("/clear-all")
    public Result<String> clearAll(@RequestHeader("token") String token) {
        if (!checkAdminPermission(token)) {
            return Result.fail("无权限：仅管理员可执行此操作");
        }
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("TRUNCATE TABLE asset_use_record");
            stmt.execute("TRUNCATE TABLE asset_inbound");
            stmt.execute("TRUNCATE TABLE asset_repair_record");
            stmt.execute("TRUNCATE TABLE asset_scrap_record");
            stmt.execute("TRUNCATE TABLE asset_info");
            stmt.execute("TRUNCATE TABLE asset_category");
            stmt.execute("TRUNCATE TABLE sys_department");
            stmt.close();
            conn.close();
            return Result.success("所有数据已清空（用户数据保留）");
        } catch (Exception e) {
            return Result.error("清空失败：" + e.getMessage());
        }
    }
}
