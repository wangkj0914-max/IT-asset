package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_custom_field_def")
public class SysCustomFieldDef {
    @TableId(type = IdType.AUTO)
    private Long fieldId;

    private String fieldName;
    private String fieldType;
    private String fieldOptions;
    private Integer isRequired;
    private Integer sortOrder;
    private String site;
    private LocalDateTime createTime;
}
