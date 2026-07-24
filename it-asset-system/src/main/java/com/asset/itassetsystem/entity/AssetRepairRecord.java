package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产维修记录实体类
 */
@Data
@TableName("asset_repair_record")
public class AssetRepairRecord {
    @TableId(type = IdType.AUTO)
    private Long repairId;
    
    private Long assetId;
    private String repairReason;
    private BigDecimal repairCost;
    private BigDecimal repairFee;
    private String repairCompany;
    private String repairContact;
    private String repairPhone;
    private Integer repairStatus; // 0-待维修 1-维修中 2-已完成
    private LocalDateTime repairDate;
    private Long applyUserId;
    private String applyUserName;
    private String applyDepartment;
    private String repairMan;
    private String remark;
    private String site;
    private LocalDateTime createTime;
}
