package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("sys_permission")
public class SysPermission {
    
    /**
     * 权限 ID
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限编码
     */
    private String permissionCode;
    
    /**
     * 权限类型：1-菜单 2-按钮/操作
     */
    private Integer permissionType;
    
    /**
     * 父权限 ID
     */
    private Long parentId;
    
    /**
     * 路径
     */
    private String path;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
