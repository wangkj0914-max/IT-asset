package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_consumable_use")
public class ConsumableUseRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    private Long consumableId;
    private String consumableName;
    private Integer quantity;
    private String applicant;
    private String department;
    private String contactPerson;
    private String usePurpose;
    private Integer approveStatus;
    private String approveUser;
    private LocalDateTime approveTime;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
