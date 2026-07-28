package com.asset.itassetsystem.dto;

import com.asset.itassetsystem.entity.AssetInventoryDetail;
import lombok.Data;
import java.util.List;

/**
 * 盘点差异报告 DTO
 */
@Data
public class InventoryReportDTO {

    /**
     * 正常数量
     */
    private int normalCount;

    /**
     * 盘盈数量
     */
    private int surplusCount;

    /**
     * 盘亏数量
     */
    private int lossCount;

    /**
     * 位置不符数量
     */
    private int locationDiffCount;

    /**
     * 状态不符数量
     */
    private int statusDiffCount;

    /**
     * 缺失数量
     */
    private int missingCount;

    /**
     * 盘盈（额外）数量
     */
    private int extraCount;

    /**
     * 差异明细列表
     */
    private List<AssetInventoryDetail> details;

    /**
     * 总数量
     */
    private int totalCount;
}
