package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_group_user")
public class SysGroupUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
}
