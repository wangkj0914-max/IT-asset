package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.asset.itassetsystem.entity.*;
import com.asset.itassetsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * 首页统计接口
 */
@RestController
@RequestMapping("/home")
public class HomeStatsController {

    @Autowired private AssetInfoService assetInfoService;
    @Autowired private AssetRepairService assetRepairService;
    @Autowired private AssetScrapService assetScrapService;
    @Autowired private AssetTransferService assetTransferService;
    @Autowired private AssetUseService assetUseService;
    @Autowired private AssetCategoryService assetCategoryService;
    @Autowired private SysDepartmentService deptService;
    @Autowired private ConsumableService consumableService;
    @Autowired private LicenseService licenseService;

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        Map<String, Long> stats = new HashMap<>();
        long total = assetInfoService.count();
        stats.put("totalAssets", total);
        stats.put("unusedAssets", assetInfoService.count(new LambdaQueryWrapper<AssetInfo>().eq(AssetInfo::getStatus, 0)));
        stats.put("usedAssets", assetInfoService.count(new LambdaQueryWrapper<AssetInfo>().eq(AssetInfo::getStatus, 1)));

        long pending = 0;
        pending += assetRepairService.count(new LambdaQueryWrapper<AssetRepairRecord>().eq(AssetRepairRecord::getRepairStatus, 0));
        pending += assetScrapService.count(new LambdaQueryWrapper<AssetScrapRecord>().eq(AssetScrapRecord::getApproveStatus, 0));
        pending += assetTransferService.count(new LambdaQueryWrapper<AssetTransferRecord>().eq(AssetTransferRecord::getTransferStatus, 0));
        pending += assetUseService.count(new LambdaQueryWrapper<AssetUseRecord>().eq(AssetUseRecord::getApproveStatus, 0));
        stats.put("pendingApprovals", pending);

        return Result.success(stats);
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam(required = false) String site) {
        Map<String, Object> data = new HashMap<>();

        // 基础统计
        data.put("totalAssets", countBySite(site));
        data.put("unusedAssets", countBySiteAndStatus(site, 0));
        data.put("usedAssets", countBySiteAndStatus(site, 1));

        // 待审批汇总
        long pending = 0;
        pending += assetRepairService.count(new LambdaQueryWrapper<AssetRepairRecord>().eq(AssetRepairRecord::getRepairStatus, 0));
        pending += assetScrapService.count(new LambdaQueryWrapper<AssetScrapRecord>().eq(AssetScrapRecord::getApproveStatus, 0));
        pending += assetTransferService.count(new LambdaQueryWrapper<AssetTransferRecord>().eq(AssetTransferRecord::getTransferStatus, 0));
        pending += assetUseService.count(new LambdaQueryWrapper<AssetUseRecord>().eq(AssetUseRecord::getApproveStatus, 0));
        data.put("pendingApprovalCount", pending);

        // 资产总值
        BigDecimal totalValue = BigDecimal.ZERO;
        for (AssetInfo a : assetInfoService.list(wrapper(site))) {
            if (a.getPurchasePrice() != null) totalValue = totalValue.add(a.getPurchasePrice());
        }
        data.put("totalAssetValue", totalValue);

        // 分类分布
        List<AssetCategory> cats = assetCategoryService.list();
        List<Map<String, Object>> catDist = new ArrayList<>();
        for (AssetCategory cat : cats) {
            var w = wrapper(site); w.eq(AssetInfo::getCategoryId, cat.getCategoryId());
            long count = assetInfoService.count(w);
            if (count > 0) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", cat.getCategoryName()); m.put("value", count);
                catDist.add(m);
            }
        }
        data.put("categoryDistribution", catDist);

        // 部门分布 Top 10
        List<Map<String, Object>> deptDist = assetInfoService.list(wrapper(site)).stream()
            .filter(a -> a.getDepartment() != null && !a.getDepartment().isEmpty())
            .collect(Collectors.groupingBy(AssetInfo::getDepartment, Collectors.counting()))
            .entrySet().stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10).map(e -> { Map<String, Object> m = new HashMap<>(); m.put("name", e.getKey()); m.put("value", e.getValue()); return m; })
            .collect(Collectors.toList());
        data.put("departmentDistribution", deptDist);

        // 低库存耗材
        List<Consumable> lowStock = consumableService.list(
            new LambdaQueryWrapper<Consumable>().apply("current_stock <= min_stock"));
        data.put("lowStockCount", (long) lowStock.size());
        data.put("lowStockItems", lowStock);

        // 即将到期维护的资产（30天内）
        List<AssetInfo> maintenanceDue = assetInfoService.list(wrapper(site)
            .isNotNull(AssetInfo::getNextMaintenanceDate)
            .le(AssetInfo::getNextMaintenanceDate, LocalDate.now().plusDays(30))
            .ge(AssetInfo::getNextMaintenanceDate, LocalDate.now()));
        data.put("maintenanceCount", (long) maintenanceDue.size());
        data.put("maintenanceItems", maintenanceDue);

        // 即将到期许可证（30天内）
        List<License> expiringLicenses = licenseService.getExpiring(30, site);
        data.put("licenseExpiringCount", (long) expiringLicenses.size());
        data.put("licenseExpiringItems", expiringLicenses);

        return Result.success(data);
    }

    private LambdaQueryWrapper<AssetInfo> wrapper(String site) {
        LambdaQueryWrapper<AssetInfo> w = new LambdaQueryWrapper<>();
        if (site != null && !site.isEmpty()) w.eq(AssetInfo::getSite, site);
        return w;
    }

    private long countBySite(String site) {
        return assetInfoService.count(wrapper(site));
    }

    private long countBySiteAndStatus(String site, Integer status) {
        var w = wrapper(site); w.eq(AssetInfo::getStatus, status);
        return assetInfoService.count(w);
    }
}
