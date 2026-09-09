package com.asset.itassetsystem.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产台账 Excel 导出 VO
 * 列顺序与前端 AssetManage.vue 表格列保持一致：
 * 状态列在「当前价值/EOL日期/保修到期/下次维护/折旧方法」之前（2026-09-09 列表列位调整后同步）
 * 采购成本列导出为 USD（purchaseCost 优先，兜底旧字段 purchasePrice）
 */
@Data
public class AssetExportVO {

    /** 序号 */
    @ExcelProperty("序号")
    private Integer index;

    /** 资产编号 */
    @ExcelProperty("资产编号")
    private String assetCode;

    /** 设备编号 */
    @ExcelProperty("设备编号")
    private String deviceNo;

    /** 资产名称 */
    @ExcelProperty("资产名称")
    private String assetName;

    /** 资产分类（由 categoryId 映射为分类名称） */
    @ExcelProperty("资产分类")
    private String categoryName;

    /** 型号 */
    @ExcelProperty("型号")
    private String model;

    /** 资产品牌 */
    @ExcelProperty("资产品牌")
    private String brand;

    /** 资产数量 */
    @ExcelProperty("资产数量")
    private Integer quantity;

    /** 购置日期 */
    @ExcelProperty("购置日期")
    private String purchaseDate;

    /** 采购成本(USD)：导出时取 purchaseCost，null 则兜底旧字段 purchasePrice */
    @ExcelProperty("采购成本(USD)")
    private BigDecimal purchaseCost;

    /** 使用部门 */
    @ExcelProperty("使用部门")
    private String department;

    /** 使用人（仅保留字段，作为责任人回退数据源；不再单独导出 Excel 列） */
    @ExcelIgnore
    private String userName;

    /** 责任人（导出列名为「使用人」） */
    @ExcelProperty("使用人")
    private String responsiblePerson;

    /** 存放地点 */
    @ExcelProperty("存放地点")
    private String storageLocation;

    /** 状态（未领用/已领用/维修中/已报废） */
    @ExcelProperty("状态")
    private String status;

    /** 当前价值（按采购成本直线折旧自动计算；是否与采购成本一同改 USD 标注待用户确认） */
    @ExcelProperty("当前价值(元)")
    private BigDecimal currentValue;

    /** EOL日期 */
    @ExcelProperty("EOL日期")
    private String eolDate;

    /** 保修到期 */
    @ExcelProperty("保修到期")
    private String warrantyExpireDate;

    /** 下次维护 */
    @ExcelProperty("下次维护")
    private String nextMaintenanceDate;

    /** 折旧方法（直线折旧/余额递减） */
    @ExcelProperty("折旧方法")
    private String depreciationMethod;

    /** 备注（去除备注中内嵌的部门信息，与前端展示一致） */
    @ExcelProperty("备注")
    private String remark;
}
