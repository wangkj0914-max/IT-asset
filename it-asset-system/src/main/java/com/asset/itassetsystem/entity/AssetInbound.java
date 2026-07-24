package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产入库记录实体类
 */
@Data
@TableName("asset_inbound")
public class AssetInbound {
    @TableId(type = IdType.AUTO)
    private Long inboundId;
    
    private String inboundNo;        // 入库单号
    private Long assetId;            // 资产 ID（审核后关联）
    private String assetName;        // 资产名称
    private Long categoryId;         // 分类 ID
    private String categoryName;     // 分类名称
    private String brand;            // 品牌
    private String model;            // 型号
    private String serialNumber;     // 序列号
    private BigDecimal purchasePrice; // 采购价格
    private String supplier;         // 供应商
    private String storageLocation;  // 存放位置
    private Integer status;          // 状态：0-待审核 1-已入库 2-已拒绝
    private String applicant;        // 申请人
    private String site;             // 站点
    private LocalDateTime applyTime; // 申请时间
    private String auditor;          // 审核人
    private LocalDateTime auditTime; // 审核时间
    private String remark;           // 备注
}
