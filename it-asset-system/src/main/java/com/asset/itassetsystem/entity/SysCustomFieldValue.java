package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_custom_field_value")
public class SysCustomFieldValue {
    @TableId(type = IdType.AUTO)
    private Long valueId;

    private Long fieldId;
    private String entityType;
    private Long entityId;
    private String fieldValue;
}
