package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_return_record")
public class AssetReturnRecord {
    @TableId(type = IdType.AUTO)
    private Long returnId;
    private Long recordId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String returnPerson;
    private String department;
    private LocalDateTime returnDate;
    private Integer conditionStatus;
    private String returnReason;
    private Integer approveStatus;
    private String approveUser;
    private LocalDateTime approveTime;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
