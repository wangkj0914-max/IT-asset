package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.*;
import com.asset.itassetsystem.mapper.*;
import com.asset.itassetsystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 综合报表接口
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired private AssetInfoService assetInfoService;
    @Autowired private AssetCategoryService assetCategoryService;
    @Autowired private AssetUseService assetUseService;
    @Autowired private ConsumableService consumableService;
    @Autowired private ConsumableRecordMapper consumableRecordMapper;
    @Autowired private AssetInventoryMapper assetInventoryMapper;
    @Autowired private AssetInventoryDetailMapper assetInventoryDetailMapper;
    @Autowired private AssetUseRecordMapper assetUseRecordMapper;
    @Autowired private ConsumableMapper consumableMapper;
    @Autowired private AssetReturnRecordMapper assetReturnRecordMapper;

    /**
     * GET /report/depreciation - 折旧概览报表
     */
    @GetMapping("/depreciation")
    public Result<Map<String, Object>> depreciation(@RequestParam(required = false) String site) {
        Map<String, Object> result = new HashMap<>();

        var wrapper = new LambdaQueryWrapper<AssetInfo>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetInfo::getSite, site);
        }
        List<AssetInfo> allAssets = assetInfoService.list(wrapper);

        BigDecimal totalOriginalValue = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        int fullyDepreciatedCount = 0;
        int eolSoonCount = 0;
        LocalDate threeMonthsLater = LocalDate.now().plusMonths(3);

        // 分类映射
        Map<Long, String> categoryMap = new HashMap<>();
        for (AssetCategory cat : assetCategoryService.list()) {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
        }

        // 按分类汇总
        Map<String, BigDecimal[]> categoryValues = new LinkedHashMap<>();

        for (AssetInfo a : allAssets) {
            BigDecimal original = a.getPurchasePrice() != null ? a.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal current = a.getCurrentValue() != null ? a.getCurrentValue() : original;

            totalOriginalValue = totalOriginalValue.add(original);
            totalCurrentValue = totalCurrentValue.add(current);

            if (current.compareTo(BigDecimal.ZERO) <= 0) {
                fullyDepreciatedCount++;
            }

            if (a.getEolDate() != null && !a.getEolDate().isAfter(threeMonthsLater) && !a.getEolDate().isBefore(LocalDate.now())) {
                eolSoonCount++;
            }

            String catName = categoryMap.getOrDefault(a.getCategoryId(), "未分类");
            BigDecimal[] vals = categoryValues.computeIfAbsent(catName, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            vals[0] = vals[0].add(original);
            vals[1] = vals[1].add(current);
        }

        BigDecimal totalDepreciation = totalOriginalValue.subtract(totalCurrentValue);
        BigDecimal depreciationRate = totalOriginalValue.compareTo(BigDecimal.ZERO) > 0
            ? totalDepreciation.divide(totalOriginalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        List<Map<String, Object>> byCategory = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : categoryValues.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("categoryName", entry.getKey());
            m.put("originalValue", entry.getValue()[0]);
            m.put("currentValue", entry.getValue()[1]);
            m.put("depreciation", entry.getValue()[0].subtract(entry.getValue()[1]));
            byCategory.add(m);
        }

        result.put("totalOriginalValue", totalOriginalValue);
        result.put("totalCurrentValue", totalCurrentValue);
        result.put("totalDepreciation", totalDepreciation);
        result.put("depreciationRate", depreciationRate);
        result.put("byCategory", byCategory);
        result.put("fullyDepreciatedCount", fullyDepreciatedCount);
        result.put("eolSoonCount", eolSoonCount);

        return Result.success(result);
    }

    /**
     * GET /report/department-summary - 部门资产汇总
     */
    @GetMapping("/department-summary")
    public Result<List<Map<String, Object>>> departmentSummary(@RequestParam(required = false) String site) {
        var wrapper = new LambdaQueryWrapper<AssetInfo>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetInfo::getSite, site);
        }
        List<AssetInfo> allAssets = assetInfoService.list(wrapper);

        Map<String, List<AssetInfo>> deptGroup = allAssets.stream()
            .filter(a -> a.getDepartment() != null && !a.getDepartment().isEmpty())
            .collect(Collectors.groupingBy(AssetInfo::getDepartment));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<AssetInfo>> entry : deptGroup.entrySet()) {
            List<AssetInfo> assets = entry.getValue();
            int count = assets.size();
            BigDecimal totalOrig = BigDecimal.ZERO;
            BigDecimal totalCurr = BigDecimal.ZERO;

            for (AssetInfo a : assets) {
                BigDecimal original = a.getPurchasePrice() != null ? a.getPurchasePrice() : BigDecimal.ZERO;
                BigDecimal current = a.getCurrentValue() != null ? a.getCurrentValue() : original;
                totalOrig = totalOrig.add(original);
                totalCurr = totalCurr.add(current);
            }

            BigDecimal avgDepRate = totalOrig.compareTo(BigDecimal.ZERO) > 0
                ? totalOrig.subtract(totalCurr).divide(totalOrig, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            Map<String, Object> m = new HashMap<>();
            m.put("department", entry.getKey());
            m.put("assetCount", count);
            m.put("totalOriginalValue", totalOrig);
            m.put("totalCurrentValue", totalCurr);
            m.put("avgDepreciationRate", avgDepRate);
            result.add(m);
        }

        result.sort((a, b) -> Integer.compare((int) b.get("assetCount"), (int) a.get("assetCount")));
        return Result.success(result);
    }

    // ==================== 高级报表 ====================

    /**
     * GET /report/asset-lifecycle - 资产全生命周期报表
     */
    @GetMapping("/asset-lifecycle")
    public Result<Map<String, Object>> assetLifecycle(@RequestParam(required = false) String site) {
        var wrapper = new LambdaQueryWrapper<AssetInfo>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetInfo::getSite, site);
        }
        List<AssetInfo> allAssets = assetInfoService.list(wrapper);

        Map<Long, String> categoryMap = new HashMap<>();
        for (AssetCategory cat : assetCategoryService.list()) {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
        }

        // status: 0-未领用 1-已领用 2-维修中 3-已报废
        // 按分类汇总
        Map<Long, int[]> catStatus = new LinkedHashMap<>(); // status counts [inStock=0, inUse=1, repairing=2, scrapped=3, total]
        Map<Long, long[]> catAgeSum = new LinkedHashMap<>();  // [sumMonths, count]
        LocalDate now = LocalDate.now();

        for (AssetInfo a : allAssets) {
            Long catId = a.getCategoryId() != null ? a.getCategoryId() : 0L;
            int[] sc = catStatus.computeIfAbsent(catId, k -> new int[5]);
            sc[4]++; // total
            if (a.getStatus() != null && a.getStatus() >= 0 && a.getStatus() <= 3) {
                sc[a.getStatus()]++;
            }

            // 计算使用月龄（从采购日期算）
            if (a.getPurchaseDate() != null) {
                long months = ChronoUnit.MONTHS.between(a.getPurchaseDate(), now);
                if (months < 0) months = 0;
                long[] as = catAgeSum.computeIfAbsent(catId, k -> new long[2]);
                as[0] += months;
                as[1]++;
            }
        }

        List<Map<String, Object>> byCategory = new ArrayList<>();
        int totalAssets = 0;
        for (Map.Entry<Long, int[]> entry : catStatus.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            String catName = categoryMap.getOrDefault(entry.getKey(), "未分类");
            int[] sc = entry.getValue();
            m.put("categoryName", catName);
            m.put("total", sc[4]);
            m.put("inStock", sc[0]);
            m.put("inUse", sc[1]);
            m.put("repairing", sc[2]);
            m.put("scrapped", sc[3]);
            long[] as = catAgeSum.get(entry.getKey());
            m.put("avgAgeMonths", as != null && as[1] > 0 ? Math.round((double) as[0] / as[1]) : 0);
            totalAssets += sc[4];
            byCategory.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("byCategory", byCategory);
        result.put("totalAssets", totalAssets);
        return Result.success(result);
    }

    /**
     * GET /report/inventory-summary - 盘点汇总报表
     */
    @GetMapping("/inventory-summary")
    public Result<Map<String, Object>> inventorySummary(@RequestParam(required = false) String site) {
        // 查询所有盘点任务（按时间倒序）
        var invWrapper = new LambdaQueryWrapper<AssetInventory>()
            .orderByDesc(AssetInventory::getCreateTime);
        if (site != null && !site.isEmpty()) {
            invWrapper.eq(AssetInventory::getSite, site);
        }
        List<AssetInventory> inventories = assetInventoryMapper.selectList(invWrapper);

        List<Map<String, Object>> recentInventories = new ArrayList<>();
        double overallAccuracySum = 0;
        int accuracyCount = 0;

        for (AssetInventory inv : inventories) {
            Map<String, Object> item = new HashMap<>();
            item.put("inventoryId", inv.getInventoryId());
            item.put("inventoryNo", inv.getInventoryNo());
            item.put("inventoryName", inv.getInventoryName());
            item.put("date", inv.getInventoryDate() != null ? inv.getInventoryDate().toString() : null);

            // 统计该盘点的明细
            var detailWrapper = new LambdaQueryWrapper<AssetInventoryDetail>()
                .eq(AssetInventoryDetail::getInventoryId, inv.getInventoryId());
            List<AssetInventoryDetail> details = assetInventoryDetailMapper.selectList(detailWrapper);

            int totalChecked = details.size();
            int normalCount = (int) details.stream().filter(d -> d.getStatus() != null && d.getStatus() == 1).count();
            int surplusCount = (int) details.stream().filter(d -> d.getStatus() != null && d.getStatus() == 2).count();
            int lossCount = (int) details.stream().filter(d -> d.getStatus() != null && d.getStatus() == 3).count();
            double completionRate = totalChecked > 0 ? Math.round((double) normalCount / totalChecked * 10000.0) / 100.0 : 0;

            item.put("totalChecked", totalChecked);
            item.put("normalCount", normalCount);
            item.put("surplusCount", surplusCount);
            item.put("lossCount", lossCount);
            item.put("completionRate", completionRate);
            recentInventories.add(item);

            overallAccuracySum += completionRate;
            accuracyCount++;
        }

        double overallAccuracy = accuracyCount > 0 ? Math.round(overallAccuracySum / accuracyCount * 100.0) / 100.0 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("recentInventories", recentInventories);
        result.put("overallAccuracy", overallAccuracy);
        return Result.success(result);
    }

    /**
     * GET /report/use-statistics - 领用归还统计
     */
    @GetMapping("/use-statistics")
    public Result<Map<String, Object>> useStatistics(@RequestParam(required = false) String site) {
        // 最近12个月
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();
        LocalDateTime twelveMonthsAgo = now.minusMonths(12).withDayOfMonth(1).atStartOfDay();

        var useWrapper = new LambdaQueryWrapper<AssetUseRecord>()
            .ge(AssetUseRecord::getCreateTime, twelveMonthsAgo);
        if (site != null && !site.isEmpty()) {
            useWrapper.eq(AssetUseRecord::getSite, site);
        }
        List<AssetUseRecord> useRecords = assetUseRecordMapper.selectList(useWrapper);

        // 初始化月度数据
        Map<String, int[]> monthlyMap = new LinkedHashMap<>();
        Map<String, long[]> monthlyApproveSum = new LinkedHashMap<>(); // [approveDaysSum, approveCount]
        for (int i = 11; i >= 0; i--) {
            String month = now.minusMonths(i).format(monthFmt);
            monthlyMap.put(month, new int[4]); // applyCount, approveCount, returnCount, overdueCount
            monthlyApproveSum.put(month, new long[2]);
        }

        int totalApply = 0;
        long totalApproveDays = 0;
        int totalApproveCount = 0;

        for (AssetUseRecord r : useRecords) {
            if (r.getCreateTime() != null) {
                String month = r.getCreateTime().format(monthFmt);
                int[] stats = monthlyMap.get(month);
                if (stats != null) {
                    // useType: 1-领用 2-归还 3-调拨
                    if (r.getUseType() != null && r.getUseType() == 1) {
                        stats[0]++; // applyCount
                        totalApply++;

                        // 审批天数
                        if (r.getApproveStatus() != null && r.getApproveStatus() == 1 && r.getApproveTime() != null) {
                            stats[1]++; // approveCount
                            long days = ChronoUnit.DAYS.between(r.getCreateTime().toLocalDate(), r.getApproveTime().toLocalDate());
                            if (days < 0) days = 0;
                            totalApproveDays += days;
                            totalApproveCount++;

                            long[] as = monthlyApproveSum.get(month);
                            if (as != null) {
                                as[0] += days;
                                as[1]++;
                            }
                        } else if (r.getApproveStatus() != null && r.getApproveStatus() == 1) {
                            stats[1]++;
                            totalApproveCount++;
                        }
                    } else if (r.getUseType() != null && r.getUseType() == 2) {
                        stats[2]++; // returnCount
                    }

                    // 逾期
                    if (r.getOverdueStatus() != null && r.getOverdueStatus() == 1) {
                        stats[3]++; // overdueCount
                    }
                }
            }
        }

        // 获取归还记录（也按12个月内）
        var returnWrapper = new LambdaQueryWrapper<AssetReturnRecord>()
            .ge(AssetReturnRecord::getCreateTime, twelveMonthsAgo);
        if (site != null && !site.isEmpty()) {
            returnWrapper.eq(AssetReturnRecord::getSite, site);
        }
        List<AssetReturnRecord> returnRecords = assetReturnRecordMapper.selectList(returnWrapper);
        for (AssetReturnRecord rr : returnRecords) {
            if (rr.getCreateTime() != null) {
                String month = rr.getCreateTime().format(monthFmt);
                int[] stats = monthlyMap.get(month);
                if (stats != null) {
                    stats[2]++; // 归还数量
                }
            }
        }

        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : monthlyMap.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("month", entry.getKey());
            m.put("applyCount", entry.getValue()[0]);
            m.put("approveCount", entry.getValue()[1]);
            m.put("returnCount", entry.getValue()[2]);
            m.put("overdueCount", entry.getValue()[3]);
            monthlyStats.add(m);
        }

        double avgApproveDays = totalApproveCount > 0 ?
            Math.round((double) totalApproveDays / totalApproveCount * 10.0) / 10.0 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyStats", monthlyStats);
        result.put("totalApply", totalApply);
        result.put("avgApproveDays", avgApproveDays);
        return Result.success(result);
    }

    /**
     * GET /report/consumable-comparison - 耗材同比环比
     */
    @GetMapping("/consumable-comparison")
    public Result<Map<String, Object>> consumableComparison(@RequestParam(required = false) String site) {
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();
        String currentMonthKey = now.format(monthFmt);
        String lastMonthKey = now.minusMonths(1).format(monthFmt);
        String sameMonthLastYearKey = now.minusYears(1).format(monthFmt);

        // 当月起始/结束
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentMonthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime sameMonthLastYearStart = now.minusYears(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime sameMonthLastYearEnd = now.minusYears(1).plusMonths(1).withDayOfMonth(1).atStartOfDay();

        // 加载所有耗材（用于获取分类和价格）
        Map<Long, Consumable> consumableMap = new HashMap<>();
        for (Consumable c : consumableService.list()) {
            consumableMap.put(c.getConsumableId(), c);
        }

        // 查询所有出库记录(type=2)，根据site筛选通过关联的耗材
        List<ConsumableRecord> allRecords = consumableRecordMapper.selectList(
            new LambdaQueryWrapper<ConsumableRecord>().eq(ConsumableRecord::getType, 2));

        // 按时间段分组
        List<ConsumableRecord> currentMonthRecords = new ArrayList<>();
        List<ConsumableRecord> lastMonthRecords = new ArrayList<>();
        List<ConsumableRecord> sameMonthLastYearRecords = new ArrayList<>();

        for (ConsumableRecord r : allRecords) {
            if (r.getCreateTime() == null) continue;
            // site筛选：通过关联耗材
            if (site != null && !site.isEmpty()) {
                Consumable con = consumableMap.get(r.getConsumableId());
                if (con == null || !site.equals(con.getSite())) continue;
            }
            if (!r.getCreateTime().isBefore(currentMonthStart) && r.getCreateTime().isBefore(currentMonthEnd)) {
                currentMonthRecords.add(r);
            } else if (!r.getCreateTime().isBefore(lastMonthStart) && r.getCreateTime().isBefore(lastMonthEnd)) {
                lastMonthRecords.add(r);
            } else if (!r.getCreateTime().isBefore(sameMonthLastYearStart) && r.getCreateTime().isBefore(sameMonthLastYearEnd)) {
                sameMonthLastYearRecords.add(r);
            }
        }

        // 计算汇总
        int[] currentStats = calcConsumableStats(currentMonthRecords, consumableMap);
        int[] lastStats = calcConsumableStats(lastMonthRecords, consumableMap);
        int[] yoyStats = calcConsumableStats(sameMonthLastYearRecords, consumableMap);

        Map<String, Object> currentMonth = new HashMap<>();
        currentMonth.put("totalQuantity", currentStats[0]);
        currentMonth.put("totalAmount", BigDecimal.valueOf(currentStats[1]).setScale(2, RoundingMode.HALF_UP));

        Map<String, Object> lastMonth = new HashMap<>();
        lastMonth.put("totalQuantity", lastStats[0]);
        lastMonth.put("totalAmount", BigDecimal.valueOf(lastStats[1]).setScale(2, RoundingMode.HALF_UP));

        Map<String, Object> sameMonthLastYear = new HashMap<>();
        sameMonthLastYear.put("totalQuantity", yoyStats[0]);
        sameMonthLastYear.put("totalAmount", BigDecimal.valueOf(yoyStats[1]).setScale(2, RoundingMode.HALF_UP));

        // 按分类统计
        Map<String, int[]> currentByCat = calcCategoryStats(currentMonthRecords, consumableMap);
        Map<String, int[]> lastByCat = calcCategoryStats(lastMonthRecords, consumableMap);

        List<Map<String, Object>> byCategory = new ArrayList<>();
        Set<String> allCats = new LinkedHashSet<>(currentByCat.keySet());
        allCats.addAll(lastByCat.keySet());

        for (String cat : allCats) {
            Map<String, Object> m = new HashMap<>();
            int curQty = currentByCat.containsKey(cat) ? currentByCat.get(cat)[0] : 0;
            int lastQty = lastByCat.containsKey(cat) ? lastByCat.get(cat)[0] : 0;
            double growth = lastQty > 0 ? Math.round((double)(curQty - lastQty) / lastQty * 10000.0) / 100.0 : 0;
            m.put("category", cat);
            m.put("currentQty", curQty);
            m.put("lastQty", lastQty);
            m.put("growth", growth);
            byCategory.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentMonth", currentMonth);
        result.put("lastMonth", lastMonth);
        result.put("sameMonthLastYear", sameMonthLastYear);
        result.put("byCategory", byCategory);
        return Result.success(result);
    }

    private int[] calcConsumableStats(List<ConsumableRecord> records, Map<Long, Consumable> consumableMap) {
        int qty = 0;
        double amount = 0;
        for (ConsumableRecord r : records) {
            int q = r.getQuantity() != null ? r.getQuantity() : 0;
            qty += q;
            Consumable con = consumableMap.get(r.getConsumableId());
            if (con != null && con.getPrice() != null) {
                amount += con.getPrice().doubleValue() * q;
            }
        }
        return new int[]{qty, (int) Math.round(amount)};
    }

    private Map<String, int[]> calcCategoryStats(List<ConsumableRecord> records, Map<Long, Consumable> consumableMap) {
        Map<String, int[]> catMap = new LinkedHashMap<>();
        for (ConsumableRecord r : records) {
            Consumable con = consumableMap.get(r.getConsumableId());
            String cat = con != null && con.getCategory() != null ? con.getCategory() : "未分类";
            int[] s = catMap.computeIfAbsent(cat, k -> new int[1]);
            s[0] += r.getQuantity() != null ? r.getQuantity() : 0;
        }
        return catMap;
    }

    /**
     * GET /report/export - 导出报表数据
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String type,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String site) {

        String csv;
        String filename;

        switch (type) {
            case "assets":
                csv = exportAssets(site);
                filename = "资产数据_" + LocalDate.now() + ".csv";
                break;
            case "depreciation":
                csv = exportDepreciation(site);
                filename = "折旧报表_" + LocalDate.now() + ".csv";
                break;
            case "inventory":
                csv = exportInventory(site);
                filename = "盘点数据_" + LocalDate.now() + ".csv";
                break;
            case "consumable":
                csv = exportConsumable(site);
                filename = "耗材数据_" + LocalDate.now() + ".csv";
                break;
            default:
                return ResponseEntity.badRequest().body("不支持的导出类型".getBytes(StandardCharsets.UTF_8));
        }

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        // 添加 BOM 使 Excel 正确识别 UTF-8
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = new byte[bom.length + bytes.length];
        System.arraycopy(bom, 0, content, 0, bom.length);
        System.arraycopy(bytes, 0, content, bom.length, bytes.length);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(content);
    }

    private String exportAssets(String site) {
        var wrapper = new LambdaQueryWrapper<AssetInfo>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetInfo::getSite, site);
        }
        List<AssetInfo> assets = assetInfoService.list(wrapper);

        Map<Long, String> categoryMap = new HashMap<>();
        for (AssetCategory cat : assetCategoryService.list()) {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("资产编号,资产名称,分类,品牌,型号,序列号,采购价格,采购日期,供应商,存放地点,状态,使用人,部门,当前价值,EOL日期\n");
        for (AssetInfo a : assets) {
            sb.append(escapeCsv(a.getAssetCode())).append(",");
            sb.append(escapeCsv(a.getAssetName())).append(",");
            sb.append(escapeCsv(categoryMap.getOrDefault(a.getCategoryId(), ""))).append(",");
            sb.append(escapeCsv(a.getBrand())).append(",");
            sb.append(escapeCsv(a.getModel())).append(",");
            sb.append(escapeCsv(a.getSerialNumber())).append(",");
            sb.append(a.getPurchasePrice() != null ? a.getPurchasePrice() : "").append(",");
            sb.append(a.getPurchaseDate() != null ? a.getPurchaseDate() : "").append(",");
            sb.append(escapeCsv(a.getSupplier())).append(",");
            sb.append(escapeCsv(a.getStorageLocation())).append(",");
            String statusStr = a.getStatus() == null ? "" :
                a.getStatus() == 0 ? "未领用" : a.getStatus() == 1 ? "已领用" : a.getStatus() == 2 ? "维修中" : a.getStatus() == 3 ? "已报废" : "";
            sb.append(statusStr).append(",");
            sb.append(escapeCsv(a.getUserName())).append(",");
            sb.append(escapeCsv(a.getDepartment())).append(",");
            sb.append(a.getCurrentValue() != null ? a.getCurrentValue() : "").append(",");
            sb.append(a.getEolDate() != null ? a.getEolDate() : "");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String exportDepreciation(String site) {
        var wrapper = new LambdaQueryWrapper<AssetInfo>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetInfo::getSite, site);
        }
        List<AssetInfo> assets = assetInfoService.list(wrapper);

        Map<Long, String> categoryMap = new HashMap<>();
        for (AssetCategory cat : assetCategoryService.list()) {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("资产编号,资产名称,分类,采购价格,当前价值,折旧金额,折旧率(%),折旧方法,折旧年限(年),EOL日期\n");
        for (AssetInfo a : assets) {
            BigDecimal original = a.getPurchasePrice() != null ? a.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal current = a.getCurrentValue() != null ? a.getCurrentValue() : original;
            BigDecimal depr = original.subtract(current);
            BigDecimal rate = original.compareTo(BigDecimal.ZERO) > 0 ?
                depr.divide(original, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            sb.append(escapeCsv(a.getAssetCode())).append(",");
            sb.append(escapeCsv(a.getAssetName())).append(",");
            sb.append(escapeCsv(categoryMap.getOrDefault(a.getCategoryId(), ""))).append(",");
            sb.append(original).append(",");
            sb.append(current).append(",");
            sb.append(depr).append(",");
            sb.append(rate).append(",");
            sb.append(escapeCsv(a.getDepreciationMethod())).append(",");
            sb.append(a.getDepreciationYears() != null ? a.getDepreciationYears() : "").append(",");
            sb.append(a.getEolDate() != null ? a.getEolDate() : "");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String exportInventory(String site) {
        // 找到最近一次盘点
        var invWrapper = new LambdaQueryWrapper<AssetInventory>().orderByDesc(AssetInventory::getCreateTime);
        if (site != null && !site.isEmpty()) {
            invWrapper.eq(AssetInventory::getSite, site);
        }
        AssetInventory latestInv = assetInventoryMapper.selectOne(invWrapper.last("LIMIT 1"));

        if (latestInv == null) {
            return "盘点单号,资产编号,资产名称,分类,存放位置,盘点状态,盘点结果,盘点人\n";
        }

        var detailWrapper = new LambdaQueryWrapper<AssetInventoryDetail>()
            .eq(AssetInventoryDetail::getInventoryId, latestInv.getInventoryId());
        List<AssetInventoryDetail> details = assetInventoryDetailMapper.selectList(detailWrapper);

        StringBuilder sb = new StringBuilder();
        sb.append("盘点单号,资产编号,资产名称,分类,存放位置,盘点状态,盘点结果,盘点人\n");
        for (AssetInventoryDetail d : details) {
            sb.append(escapeCsv(latestInv.getInventoryNo())).append(",");
            sb.append(escapeCsv(d.getAssetCode())).append(",");
            sb.append(escapeCsv(d.getAssetName())).append(",");
            sb.append(escapeCsv(d.getCategoryName())).append(",");
            sb.append(escapeCsv(d.getStorageLocation())).append(",");
            String statusStr = d.getStatus() == null ? "" :
                d.getStatus() == 0 ? "待盘点" : d.getStatus() == 1 ? "正常" : d.getStatus() == 2 ? "盘盈" : d.getStatus() == 3 ? "盘亏" : "";
            sb.append(statusStr).append(",");
            sb.append(escapeCsv(d.getResultRemark())).append(",");
            sb.append(escapeCsv(d.getCheckerName()));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String exportConsumable(String site) {
        // 出库记录
        List<ConsumableRecord> records = consumableRecordMapper.selectList(
            new LambdaQueryWrapper<ConsumableRecord>().eq(ConsumableRecord::getType, 2).orderByDesc(ConsumableRecord::getCreateTime));

        Map<Long, Consumable> consumableMap = new HashMap<>();
        for (Consumable c : consumableService.list()) {
            consumableMap.put(c.getConsumableId(), c);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("耗材名称,分类,单位,出库数量,价格,金额,操作人,出库时间\n");
        for (ConsumableRecord r : records) {
            Consumable con = consumableMap.get(r.getConsumableId());
            if (site != null && !site.isEmpty() && con != null && !site.equals(con.getSite())) continue;
            sb.append(escapeCsv(con != null ? con.getConsumableName() : "")).append(",");
            sb.append(escapeCsv(con != null ? con.getCategory() : "")).append(",");
            sb.append(escapeCsv(con != null ? con.getUnit() : "")).append(",");
            sb.append(r.getQuantity() != null ? r.getQuantity() : "").append(",");
            sb.append(con != null && con.getPrice() != null ? con.getPrice() : "").append(",");
            if (con != null && con.getPrice() != null && r.getQuantity() != null) {
                sb.append(con.getPrice().multiply(BigDecimal.valueOf(r.getQuantity()))).append(",");
            } else {
                sb.append(",");
            }
            sb.append(escapeCsv(r.getOperatorName())).append(",");
            sb.append(r.getCreateTime() != null ? r.getCreateTime() : "");
            sb.append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /**
     * GET /consumable/trend - 耗材消耗趋势（在 ConsumableController 实现实际请求）
     * 这里提供一个辅助方法供 dashboard 使用
     */
    public static List<Map<String, Object>> getMonthlyConsumption(
            com.asset.itassetsystem.mapper.ConsumableRecordMapper mapper,
            int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (months <= 0) months = 12;

        LocalDateTime since = LocalDateTime.now().minusMonths(months).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<ConsumableRecord> records = mapper.selectList(
            new LambdaQueryWrapper<ConsumableRecord>()
                .eq(ConsumableRecord::getType, 2)
                .ge(ConsumableRecord::getCreateTime, since));

        Map<String, int[]> monthlyMap = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            monthlyMap.put(LocalDate.now().minusMonths(i).format(monthFmt), new int[]{0});
        }

        for (ConsumableRecord r : records) {
            if (r.getCreateTime() != null) {
                String key = r.getCreateTime().format(monthFmt);
                int[] q = monthlyMap.get(key);
                if (q != null) q[0] += r.getQuantity() != null ? r.getQuantity() : 0;
            }
        }

        for (Map.Entry<String, int[]> entry : monthlyMap.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", entry.getKey());
            m.put("totalQuantity", entry.getValue()[0]);
            result.add(m);
        }
        return result;
    }
}
