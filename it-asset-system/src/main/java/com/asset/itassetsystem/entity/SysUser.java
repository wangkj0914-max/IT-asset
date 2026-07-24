package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHashType;
    private Integer loginFailCount;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime lockedUntil;
    private String realName;
    private String email;
    private String phone;
    private String department;
    private String site;
    private Integer role; // 1-普通用户 2-管理员
    private Integer status; // 0-禁用 1-正常
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
