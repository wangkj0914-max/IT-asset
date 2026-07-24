package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_consumable")
public class Consumable {
    @TableId(type = IdType.AUTO)
    private Long consumableId;
    private String consumableName;
    private String category;
    private String unit;
    private Integer currentStock;
    private Integer minStock;
    private BigDecimal price;
    private String supplier;
    private String remark;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
