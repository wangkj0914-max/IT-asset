package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_contract")
public class SysContract {
    @TableId(type = IdType.AUTO)
    private Long contractId;
    private String contractNo;
    private String contractName;
    private String supplier;
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private Integer status;
    private String site;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
