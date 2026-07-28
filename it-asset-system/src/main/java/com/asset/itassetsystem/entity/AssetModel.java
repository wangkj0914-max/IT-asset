package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产模型实体类
 * 统一管理同规格资产的默认参数（EOL、折旧等）
 */
@Data
@TableName("asset_model")
public class AssetModel {
    @TableId(type = IdType.AUTO)
    private Long modelId;

    private String modelName;        // 模型名称
    private String modelNumber;      // 模型编号
    private Long categoryId;         // 所属分类ID
    private String manufacturer;     // 制造商
    private String specs;            // 规格说明(CPU/RAM/HDD等)
    private Integer eolMonths;       // 默认EOL周期(月)
    private Integer depreciationYears; // 默认折旧年限
    private String depreciationMethod; // 折旧方法: straight_line / declining_balance
    private String site;             // 所属站点
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
