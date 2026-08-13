package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.InventoryCreateDTO;
import com.asset.itassetsystem.dto.InventoryReportDTO;
import com.asset.itassetsystem.entity.AssetInventory;
import com.asset.itassetsystem.entity.AssetInventoryDetail;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产盘点控制器
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/inventory")
public class AssetInventoryController {
    
    @Autowired
    private AssetInventoryService assetInventoryService;

    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 扫码快速查找资产（用于移动盘点）
     */
    @GetMapping("/scan")
    public Result<Map<String, Object>> scanAsset(@RequestParam String code) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssetInfo>()
            .eq(AssetInfo::getAssetCode, code);
        AssetInfo asset = assetInfoService.getOne(wrapper);
        if (asset == null) {
            return Result.fail("未找到资产: " + code);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("assetId", asset.getAssetId());
        data.put("assetCode", asset.getAssetCode());
        data.put("assetName", asset.getAssetName());
        data.put("brand", asset.getBrand());
        data.put("model", asset.getModel());
        data.put("status", asset.getStatus());
        data.put("location", asset.getStorageLocation());
        data.put("currentUser", asset.getUserName());
        data.put("department", asset.getDepartment());
        return Result.success(data);
    }
    
    /**
     * 创建盘点任务
     */
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody InventoryCreateDTO dto) {
        try {
            assetInventoryService.createInventory(dto);
            return Result.success("盘点任务创建成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
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
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 执行盘点（单条明细）
     */
    @PostMapping("/check")
    public Result<String> check(@RequestParam Long detailId,
                                @RequestParam Integer status,
                                @RequestParam(required = false) String remark,
                                @RequestParam(required = false) String actualLocation,
                                @RequestParam(required = false) String differenceType) {
        try {
            assetInventoryService.checkInventory(detailId, status, remark, actualLocation, differenceType);
            return Result.success("盘点完成");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
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
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 生成盘点差异报告
     */
    @GetMapping("/report")
    public Result<InventoryReportDTO> report(@RequestParam Long inventoryId) {
        try {
            InventoryReportDTO report = assetInventoryService.generateReport(inventoryId);
            return Result.success(report);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取盘点任务详情
     */
    @GetMapping("/detail")
    public Result<AssetInventory> detail(@RequestParam Long inventoryId) {
        AssetInventory inventory = assetInventoryService.getDetail(inventoryId);
        if (inventory == null) {
            return Result.fail("盘点任务不存在");
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
