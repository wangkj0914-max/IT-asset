package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_group_permission")
public class SysGroupPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private String permission;
}
