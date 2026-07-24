package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产分类实体类
 */
@Data
@TableName("asset_category")
public class AssetCategory {
    @TableId(type = IdType.AUTO)
    private Long categoryId;
    
    private String categoryName;
    private Long parentId;
    private Integer sortOrder;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
