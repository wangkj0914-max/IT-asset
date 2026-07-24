package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 数据库初始化控制器（仅管理员可操作）
 */
@RestController
@RequestMapping("/system-init")
public class SystemInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建入库表
     */
    @PostMapping("/create-inbound-table")
    public Result<String> createInboundTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `asset_inbound` (" +
                "`inbound_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库 ID'," +
                "`inbound_no` VARCHAR(50) NOT NULL COMMENT '入库单号'," +
                "`asset_id` BIGINT DEFAULT NULL COMMENT '资产 ID'," +
                "`asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称'," +
                "`category_id` BIGINT DEFAULT NULL COMMENT '分类 ID'," +
                "`brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌'," +
                "`model` VARCHAR(200) DEFAULT NULL COMMENT '型号'," +
                "`serial_number` VARCHAR(100) DEFAULT NULL COMMENT '序列号'," +
                "`purchase_price` DECIMAL(10,2) DEFAULT NULL COMMENT '采购价格'," +
                "`supplier` VARCHAR(200) DEFAULT NULL COMMENT '供应商'," +
                "`storage_location` VARCHAR(200) DEFAULT NULL COMMENT '存放位置'," +
                "`status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已入库 2-已拒绝'," +
                "`applicant` VARCHAR(50) DEFAULT NULL COMMENT '申请人'," +
                "`apply_time` DATETIME DEFAULT NULL COMMENT '申请时间'," +
                "`auditor` VARCHAR(50) DEFAULT NULL COMMENT '审核人'," +
                "`audit_time` DATETIME DEFAULT NULL COMMENT '审核时间'," +
                "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注'," +
                "PRIMARY KEY (`inbound_id`)," +
                "UNIQUE KEY `uk_inbound_no` (`inbound_no`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci";
            
            jdbcTemplate.execute(sql);
            return Result.success("资产入库表创建成功！");
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 检查表是否存在
     */
    @GetMapping("/check-inbound-table")
    public Result<Boolean> checkInboundTable() {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'asset_inbound'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return Result.success(count != null && count > 0);
        } catch (Exception e) {
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 修复入库表字段
     */
    @PostMapping("/fix-inbound-table")
    public Result<String> fixInboundTable() {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asset_inbound' " +
                "AND COLUMN_NAME = 'category_name'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count > 0) {
                return Result.success("字段已存在，无需修复");
            }
            
            jdbcTemplate.execute("ALTER TABLE asset_inbound " +
                "ADD COLUMN `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称' AFTER `category_id`");
            
            return Result.success("表结构修复成功！");
        } catch (Exception e) {
            return Result.error("修复失败：" + e.getMessage());
        }
    }
}
