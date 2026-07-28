package com.asset.itassetsystem.entity;

import com.asset.itassetsystem.config.FlexibleLocalDateDeserializer;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产信息实体类
 */
@Data
@TableName("asset_info")
public class AssetInfo {
    @TableId(type = IdType.AUTO)
    private Long assetId;

    private String site; // 站点
    private String assetCode; // 资产编号

    @NotBlank(message = "资产名称不能为空")
    private String assetName;

    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;

    private String brand;
    private String model;
    private Long modelId; // 资产模型ID (P0新增)
    private String serialNumber;
    private BigDecimal purchasePrice;
    private BigDecimal purchaseCost; // 采购成本 (P0新增)
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate purchaseDate;
    private String supplier;
    private String storageLocation;

    @NotNull(message = "资产状态不能为空")
    private Integer status; // 0-未领用 1-已领用 2-维修中 3-已报废

    private Long statusLabelId; // 状态标签ID (P0新增)

    private Long userId; // 当前使用人 ID
    private String userName; // 使用人姓名
    private String department; // 所属部门
    private String warrantyInfo; // 维保信息
    private String remark;

    // 新增字段
    private String assetImage; // 资产图片URL
    private Integer quantity; // 资产数量
    private String depreciationMethod; // 折旧方法
    private Integer depreciationYears; // 折旧年限 (P0新增)
    private BigDecimal depreciationRate; // 年折旧率(%) (P0新增)
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate eolDate; // EOL日期 (P0新增)
    private BigDecimal currentValue; // 当前价值(自动计算) (P0新增)
    private String responsiblePerson; // 责任人
    private LocalDate nextMaintenanceDate;
    private Integer maintenanceCycleDays;
    private LocalDate warrantyExpireDate;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
