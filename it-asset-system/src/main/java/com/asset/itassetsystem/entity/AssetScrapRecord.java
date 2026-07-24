package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产报废记录实体类
 */
@Data
@TableName("asset_scrap_record")
public class AssetScrapRecord {
    @TableId(type = IdType.AUTO)
    private Long scrapId;
    
    private Long assetId;
    private String assetCode;      // 资产编号（冗余字段，方便查询）
    private String assetName;      // 资产名称（冗余字段，方便查询）
    private String scrapReason;
    private Integer scrapType; // 0-正常报废 1-损坏报废 2-丢失报废
    private BigDecimal originalPrice;
    private BigDecimal residualValue;
    private String applyUserName;  // 申请人姓名
    private String applyDepartment; // 申请部门
    private String approveUser;
    private Integer approveStatus; // 0-待审批 1-已通过 2-已拒绝
    private LocalDateTime approveTime;
    private String remark;
    private String site;
    private LocalDateTime createTime;
}
