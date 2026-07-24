package com.asset.itassetsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建盘点任务 DTO
 * 支持多维度筛选：资产类别、存放地点、资产状态、使用人等
 */
@Data
public class InventoryCreateDTO {
    
    /**
     * 盘点名称
     */
    @NotBlank(message = "盘点名称不能为空")
    private String inventoryName;
    
    /**
     * 盘点范围类型
     * 0-全部资产
     * 1-按资产分类筛选
     * 2-按存放地点筛选
     * 3-按资产状态筛选
     * 4-按使用人筛选
     * 5-组合条件筛选
     * 9-指定资产列表
     */
    @NotNull(message = "盘点范围不能为空")
    private Integer inventoryRange;
    
    /**
     * 资产分类 ID 列表（范围=1 时使用）
     */
    private List<Long> categoryIds;
    
    /**
     * 存放地点列表（范围=2 时使用）
     */
    private List<String> storageLocations;
    
    /**
     * 资产状态列表（范围=3 时使用）
     * 0-闲置 1-在用 2-维修中 3-已报废
     */
    private List<Integer> assetStatuses;
    
    /**
     * 使用人 ID 列表（范围=4 时使用）
     */
    private List<Long> userIds;
    
    /**
     * 组合筛选条件描述（范围=5 时使用）
     */
    private String rangeValue;
    
    /**
     * 资产 ID 列表（范围=9 时使用）
     */
    private List<Long> assetIds;
    
    /**
     * 备注
     */
    private String remark;
}
