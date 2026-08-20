package com.asset.itassetsystem.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产台账 Excel 导出 VO
 * 字段与前端 AssetManage.vue 表格列保持一致（含 型号/使用人 等明细字段）
 */
@Data
public class AssetExportVO {

    /** 序号 */
    @ExcelProperty("序号")
    private Integer index;

    /** 资产编号 */
    @ExcelProperty("资产编号")
    private String assetCode;

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

    /** 原始价值（元） */
    @ExcelProperty("原始价值(元)")
    private BigDecimal purchasePrice;

    /** 当前价值（元） */
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

    /** 使用部门 */
    @ExcelProperty("使用部门")
    private String department;

    /** 使用人 */
    @ExcelProperty("使用人")
    private String userName;

    /** 责任人 */
    @ExcelProperty("责任人")
    private String responsiblePerson;

    /** 存放地点 */
    @ExcelProperty("存放地点")
    private String storageLocation;

    /** 状态（未领用/已领用/维修中/已报废） */
    @ExcelProperty("状态")
    private String status;

    /** 备注（去除备注中内嵌的部门信息，与前端展示一致） */
    @ExcelProperty("备注")
    private String remark;
}
