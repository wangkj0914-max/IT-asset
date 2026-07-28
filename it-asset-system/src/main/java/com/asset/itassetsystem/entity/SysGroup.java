package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_group")
public class SysGroup {
    @TableId(type = IdType.AUTO)
    private Long groupId;
    private String groupName;
    private String description;
    private String site;
    private LocalDateTime createTime;
}
