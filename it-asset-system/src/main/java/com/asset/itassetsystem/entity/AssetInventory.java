package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产盘点任务实体
 */
@Data
@TableName("asset_inventory")
public class AssetInventory {
    
    /**
     * 盘点 ID
     */
    @TableId(value = "inventory_id", type = IdType.AUTO)
    private Long inventoryId;
    
    /**
     * 盘点单号
     */
    private String inventoryNo;
    
    /**
     * 盘点名称
     */
    private String inventoryName;
    
    /**
     * 盘点范围类型
     * 0-全部资产
     * 1-按部门筛选
     * 2-按资产分类筛选
     * 3-按存放地点筛选
     * 4-按资产状态筛选
     * 5-组合条件筛选
     * 9-指定资产列表
     */
    private Integer inventoryRange;
    
    /**
     * 盘点范围描述
     */
    private String rangeValue;
    
    /**
     * 盘点日期
     */
    private LocalDateTime inventoryDate;
    
    /**
     * 盘点人 ID
     */
    private Long operatorId;
    
    /**
     * 盘点人姓名
     */
    private String operatorName;
    
    /**
     * 盘点状态：0-待盘点 1-盘点中 2-已完成
     */
    private Integer status;
    
    /**
     * 盘盈数量
     */
    private Integer surplusCount;
    
    /**
     * 盘亏数量
     */
    private Integer lossCount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private String site;
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
