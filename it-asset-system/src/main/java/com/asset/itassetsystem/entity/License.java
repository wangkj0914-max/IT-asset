package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_license")
public class License {
    @TableId(type = IdType.AUTO)
    private Long licenseId;
    private String softwareName;
    private String vendor;
    private String licenseKey;
    private Integer totalCount;
    private Integer usedCount;
    private LocalDate expireDate;
    private BigDecimal unitPrice;
    private String responsiblePerson;
    private String remark;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
