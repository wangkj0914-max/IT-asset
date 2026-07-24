package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产盘点明细实体
 */
@Data
@TableName("asset_inventory_detail")
public class AssetInventoryDetail {
    
    /**
     * 明细 ID
     */
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Long detailId;
    
    /**
     * 盘点 ID
     */
    private Long inventoryId;
    
    /**
     * 资产 ID
     */
    private Long assetId;
    
    /**
     * 资产编号
     */
    private String assetCode;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产分类
     */
    private String categoryName;
    
    /**
     * 存放位置
     */
    private String storageLocation;
    
    /**
     * 使用部门
     */
    private String department;
    
    /**
     * 使用人
     */
    private String userName;
    
    /**
     * 盘点状态：0-待盘点 1-正常 2-盘盈 3-盘亏
     */
    private Integer status;
    
    /**
     * 盘点结果说明
     */
    private String resultRemark;
    
    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 盘点人
     */
    private String checkerName;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
