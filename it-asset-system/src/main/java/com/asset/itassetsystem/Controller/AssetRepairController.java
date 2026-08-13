package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.RepairApplyDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetRepairRecord;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetRepairService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资产维修控制器
 */
@RestController
@RequestMapping("/repair")
public class AssetRepairController {

    @Autowired
    private AssetRepairService assetRepairService;

    @Autowired
    private AssetInfoService assetInfoService;



    /**
     * 报修申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@Valid @RequestBody RepairApplyDTO dto) {
        try {
            assetRepairService.apply(dto);
            return Result.success("报修申请提交成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新维修状态
     */
    @PostMapping("/update-status")
    public Result<String> updateStatus(@RequestParam Long repairId,
                                       @RequestParam Integer status,
                                       @RequestParam(required = false) String repairMan) {
        try {
            assetRepairService.updateStatus(repairId, status, repairMan);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 完成维修
     */
    @PostMapping("/complete")
    public Result<String> complete(@RequestParam Long repairId,
                                   @RequestParam(required = false) BigDecimal cost,
                                   @RequestParam(required = false) String remark) {
        try {
            assetRepairService.complete(repairId, cost, remark);
            return Result.success("维修已完成");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询资产的维修记录
     */
    @GetMapping("/list")
    public Result<List<AssetRepairRecord>> listByAssetId(@RequestParam Long assetId) {
        List<AssetRepairRecord> list = assetRepairService.listByAssetId(assetId);
        return Result.success(list);
    }

    /**
     * 分页查询维修记录
     */
    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String site) {

        Page<AssetRepairRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetRepairRecord> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(AssetRepairRecord::getRepairStatus, status);
        }
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetRepairRecord::getSite, site);
        }

        wrapper.orderByDesc(AssetRepairRecord::getCreateTime);

        IPage<AssetRepairRecord> repairPage = assetRepairService.page(page, wrapper);

        // 批量查询关联的资产信息，避免 N+1 问题
        Set<Long> assetIds = repairPage.getRecords().stream()
                .map(AssetRepairRecord::getAssetId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, AssetInfo> assetMap = new java.util.HashMap<>();
        if (!assetIds.isEmpty()) {
            List<AssetInfo> assets = assetInfoService.listByIds(new ArrayList<>(assetIds));
            for (AssetInfo a : assets) {
                assetMap.put(a.getAssetId(), a);
            }
        }

        // 补充资产名称、报修人、部门等字段
        List<Map<String, Object>> records = repairPage.getRecords().stream().map(record -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("repairId", record.getRepairId());
            map.put("assetId", record.getAssetId());
            map.put("repairReason", record.getRepairReason());
            map.put("repairCost", record.getRepairCost());
            map.put("repairStatus", record.getRepairStatus());
            map.put("remark", record.getRemark());
            map.put("createTime", record.getCreateTime());

            // 从批量查询结果中获取资产名称
            AssetInfo asset = assetMap.get(record.getAssetId());
            map.put("assetName", asset != null ? asset.getAssetName() : "未知");
            map.put("assetCode", asset != null ? asset.getAssetCode() : "");

            // 报修人
            map.put("applyUserName", record.getApplyUserName() != null ? record.getApplyUserName() : "");
            map.put("applyDepartment", record.getApplyDepartment() != null ? record.getApplyDepartment() : "");

            return map;
        }).collect(Collectors.toList());

        // 如果前端传了 assetName 做过滤，在内存中过滤
        if (StringUtils.hasText(assetName)) {
            records = records.stream()
                .filter(m -> ((String) m.get("assetName")).contains(assetName))
                .collect(Collectors.toList());
        }

        // 包装返回
        IPage<Map<String, Object>> result = new Page<>(pageNum, pageSize, repairPage.getTotal());
        result.setRecords(records);
        return Result.success(result);
    }
}
