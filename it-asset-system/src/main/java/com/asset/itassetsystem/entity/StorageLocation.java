package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_storage_location")
public class StorageLocation {
    @TableId(type = IdType.AUTO)
    private Long locationId;
    private String site;
    private String locationName;
    private String remark;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
