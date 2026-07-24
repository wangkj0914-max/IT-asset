package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_consumable_record")
public class ConsumableRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    private Long consumableId;
    private Integer type;
    private Integer quantity;
    private String operatorName;
    private String remark;
    private LocalDateTime createTime;
}
