package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产调拨记录实体
 */
@Data
@TableName("asset_transfer_record")
public class AssetTransferRecord {
    @TableId(type = IdType.AUTO)
    private Long transferId;
    
    private String transferNo;
    private Long assetId;
    private String assetName;
    private String assetCode;
    private String fromDepartment;
    private String fromLocation;
    private String fromUser;
    private String toDepartment;
    private String toLocation;
    private String toUser;
    private String transferReason;
    private Integer transferStatus; // 0-待审批 1-已通过 2-已拒绝
    private Long applyUserId;
    private String applyUserName;
    private Long approveUserId;
    private String approveUserName;
    private LocalDateTime approveTime;
    private String approveRemark;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
