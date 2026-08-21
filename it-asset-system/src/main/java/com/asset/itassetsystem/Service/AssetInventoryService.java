package com.asset.itassetsystem.service;

import com.asset.itassetsystem.dto.InventoryCreateDTO;
import com.asset.itassetsystem.dto.InventoryReportDTO;
import com.asset.itassetsystem.entity.AssetInventory;
import com.asset.itassetsystem.entity.AssetInventoryDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 资产盘点服务接口
 */
public interface AssetInventoryService extends IService<AssetInventory> {
    
    /**
     * 创建盘点任务
     */
    void createInventory(InventoryCreateDTO dto);
    
    /**
     * 更新盘点任务状态
     */
    void updateInventoryStatus(Long inventoryId, Integer status);
    
    /**
     * 执行盘点（单条明细）
     */
    void checkInventory(Long detailId, Integer status, String remark, String actualLocation, String differenceType);

    /**
     * 生成盘点差异报告
     */
    InventoryReportDTO generateReport(Long inventoryId);
    
    /**
     * 完成盘点任务
     */
    void finishInventory(Long inventoryId);
    
    /**
     * 获取盘点任务详情
     */
    AssetInventory getDetail(Long inventoryId);
    
    /**
     * 获取盘点明细列表
     */
    List<AssetInventoryDetail> listDetails(Long inventoryId);
    
    /**
     * 分页查询盘点任务
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   盘点状态（可选）
     * @param site     站点（可选，站点隔离过滤）
     */
    Object listPage(Integer pageNum, Integer pageSize, Integer status, String site);
}
