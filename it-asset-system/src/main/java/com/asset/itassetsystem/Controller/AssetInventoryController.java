package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.InventoryCreateDTO;
import com.asset.itassetsystem.entity.AssetInventory;
import com.asset.itassetsystem.entity.AssetInventoryDetail;
import com.asset.itassetsystem.service.AssetInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资产盘点控制器
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/inventory")
public class AssetInventoryController {
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    /**
     * 创建盘点任务
     */
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody InventoryCreateDTO dto) {
        try {
            assetInventoryService.createInventory(dto);
            return Result.success("盘点任务创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新盘点任务状态（开始盘点）
     */
    @PostMapping("/update")
    public Result<String> update(@RequestParam Long inventoryId,
                                 @RequestParam Integer status) {
        try {
            assetInventoryService.updateInventoryStatus(inventoryId, status);
            return Result.success("盘点任务状态已更新");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 执行盘点（单条明细）
     */
    @PostMapping("/check")
    public Result<String> check(@RequestParam Long detailId,
                                @RequestParam Integer status,
                                @RequestParam(required = false) String remark) {
        try {
            assetInventoryService.checkInventory(detailId, status, remark);
            return Result.success("盘点完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 完成盘点任务
     */
    @PostMapping("/finish")
    public Result<String> finish(@RequestParam Long inventoryId) {
        try {
            assetInventoryService.finishInventory(inventoryId);
            return Result.success("盘点任务已完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取盘点任务详情
     */
    @GetMapping("/detail")
    public Result<AssetInventory> detail(@RequestParam Long inventoryId) {
        AssetInventory inventory = assetInventoryService.getDetail(inventoryId);
        if (inventory == null) {
            return Result.error("盘点任务不存在");
        }
        return Result.success(inventory);
    }
    
    /**
     * 获取盘点明细列表
     */
    @GetMapping("/details")
    public Result<List<AssetInventoryDetail>> details(@RequestParam Long inventoryId) {
        List<AssetInventoryDetail> list = assetInventoryService.listDetails(inventoryId);
        return Result.success(list);
    }
    
    /**
     * 分页查询盘点任务
     */
    @GetMapping("/page")
    public Result<Object> listPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String site) {
        Object result = assetInventoryService.listPage(pageNum, pageSize, status);
        if (site != null && !site.isEmpty() && result != null) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<Object> records = (java.util.List<Object>) result.getClass().getMethod("getRecords").invoke(result);
                if (records != null) {
                    records.removeIf(r -> { try { return !site.equals(r.getClass().getMethod("getSite").invoke(r)); } catch(Exception e) { return false; } });
                    result.getClass().getMethod("setTotal", long.class).invoke(result, (long) records.size());
                }
            } catch(Exception ignored) {}
        }
        return Result.success(result);
    }
}
