package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_asset_change_log")
public class AssetChangeLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long assetId;
    private String assetCode;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String operator;
    private LocalDateTime changeTime;
}
