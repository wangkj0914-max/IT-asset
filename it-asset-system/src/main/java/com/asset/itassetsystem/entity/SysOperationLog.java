package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String userName;
    private String module;
    private String operation;
    private String method;
    private String requestUri;
    private String requestParams;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime createTime;
}
