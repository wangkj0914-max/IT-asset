package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.BatchUpdateDTO;
import com.asset.itassetsystem.entity.AssetChangeLog;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetModel;
import com.asset.itassetsystem.mapper.AssetChangeLogMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产信息控制器
 */
@RestController
@RequestMapping("/assetInfo")
public class AssetInfoController {

    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired
    private AssetChangeLogMapper changeLogMapper;

    @Autowired
    private AssetModelService assetModelService;

    @Autowired
    private javax.servlet.http.HttpServletRequest httpRequest;

    /**
     * 资产入库（新增资产）
     */
    @PostMapping("/save")
    public Result<Map<String, Object>> saveAsset(@RequestBody AssetInfo assetInfo) {
        if (assetInfo.getAssetName() == null || assetInfo.getAssetName().trim().isEmpty()) {
            return Result.error("资产名称不能为空");
        }
        if (assetInfo.getCategoryId() == null) {
            return Result.error("资产分类不能为空");
        }
        
        // 资产编号：前端传入则使用，否则自动生成
        if (assetInfo.getAssetCode() == null || assetInfo.getAssetCode().trim().isEmpty()) {
            assetInfo.setAssetCode(generateAssetCode());
        }
        assetInfo.setCreateTime(LocalDateTime.now());
        // 自动设置站点（从请求参数读取）
        if (assetInfo.getSite() == null || assetInfo.getSite().isEmpty()) {
            String site = httpRequest.getParameter("site");
            assetInfo.setSite(site != null && !site.isEmpty() ? site : "苏州");
        }
        
        // 如果选择了资产模型，从模型继承折旧参数
        applyModelDefaults(assetInfo);
        // 自动计算折旧和当前价值
        calculateDepreciation(assetInfo);
        // 自动推算下次维护日期
        calculateMaintenance(null, assetInfo);
        
        boolean save = assetInfoService.save(assetInfo);
        if (save) {
            // 返回资产ID供自定义字段关联
            Map<String, Object> data = new HashMap<>();
            data.put("assetId", assetInfo.getAssetId());
            data.put("assetCode", assetInfo.getAssetCode());
            return Result.success(data);
        } else {
            return Result.error("资产入库失败");
        }
    }

    /**
     * 查询所有资产（不分页）
     */
    @GetMapping("/list")
    public Result<List<AssetInfo>> listAsset() {
        List<AssetInfo> list = assetInfoService.list();
        return Result.success(list);
    }
    
    /**
     * 分页查询资产（支持搜索和筛选）
     * @param current 当前页
     * @param size 每页大小
     * @param assetName 资产名称（模糊搜索）
     * @param categoryId 分类 ID
     * @param status 状态
     */
    @GetMapping("/page")
    public Result<IPage<AssetInfo>> pageAsset(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String assetCode,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String responsiblePerson,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tagNo,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Long statusLabelId,
            @RequestParam(required = false) String sortColumn,
            @RequestParam(required = false) String sortOrder) {

        Page<AssetInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<AssetInfo> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（资产名称或编号）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AssetInfo::getAssetName, keyword)
                              .or()
                              .like(AssetInfo::getAssetCode, keyword));
        }

        // 标签号搜索（序列号或资产编号）
        if (StringUtils.hasText(tagNo)) {
            wrapper.and(w -> w.like(AssetInfo::getSerialNumber, tagNo)
                              .or()
                              .like(AssetInfo::getAssetCode, tagNo));
        }

        // 模糊搜索资产名称
        if (StringUtils.hasText(assetName)) {
            wrapper.like(AssetInfo::getAssetName, assetName);
        }

        // 模糊搜索资产编号
        if (StringUtils.hasText(assetCode)) {
            wrapper.like(AssetInfo::getAssetCode, assetCode);
        }

        // 精确匹配存放位置
        if (StringUtils.hasText(storageLocation)) {
            wrapper.eq(AssetInfo::getStorageLocation, storageLocation);
        }

        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(AssetInfo::getCategoryId, categoryId);
        }

        // 按状态筛选
        if (status != null) {
            wrapper.eq(AssetInfo::getStatus, status);
        }

        // 按部门筛选
        if (StringUtils.hasText(department)) {
            wrapper.eq(AssetInfo::getDepartment, department);
        }

        // 按责任人筛选（模糊匹配：同时查 responsible_person 和 user_name）
        if (StringUtils.hasText(responsiblePerson)) {
            wrapper.and(w -> w.like(AssetInfo::getResponsiblePerson, responsiblePerson)
                .or().like(AssetInfo::getUserName, responsiblePerson));
        }
        
        // 按站点筛选
        if (StringUtils.hasText(site)) {
            wrapper.eq(AssetInfo::getSite, site);
        }

        // 按资产模型筛选
        if (modelId != null) {
            wrapper.eq(AssetInfo::getModelId, modelId);
        }

        // 按状态标签筛选
        if (statusLabelId != null) {
            wrapper.eq(AssetInfo::getStatusLabelId, statusLabelId);
        }
        
        // 动态排序，默认按创建时间倒序
        if (StringUtils.hasText(sortColumn)) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            String dbColumn = switch (sortColumn) {
                case "assetName" -> "asset_name";
                case "assetCode" -> "asset_code";
                case "purchasePrice" -> "purchase_price";
                case "purchaseCost" -> "purchase_cost";
                case "currentValue" -> "current_value";
                case "eolDate" -> "eol_date";
                case "createTime" -> "create_time";
                case "status" -> "status";
                case "department" -> "department";
                default -> "create_time";
            };
            wrapper.last("ORDER BY " + dbColumn + (asc ? " ASC" : " DESC"));
        } else {
            wrapper.orderByDesc(AssetInfo::getCreateTime);
        }
        
        IPage<AssetInfo> result = assetInfoService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 批量导入资产
     */
    @PostMapping("/batchSave")
    public Result<String> batchSaveAsset(@RequestBody List<AssetInfo> assetList) {
        if (assetList == null || assetList.isEmpty()) {
            return Result.error("导入数据不能为空");
        }
        int success = 0;
        for (AssetInfo asset : assetList) {
            if (asset.getAssetName() == null || asset.getAssetName().trim().isEmpty()) continue;
            if (asset.getCategoryId() == null) continue;
            // 手动有编号则保留，无则自动生成
            if (asset.getAssetCode() == null || asset.getAssetCode().trim().isEmpty()) {
                asset.setAssetCode(generateAssetCode());
            }
            asset.setCreateTime(LocalDateTime.now());
            if (asset.getStatus() == null) asset.setStatus(0);
            if (asset.getQuantity() == null) asset.setQuantity(1);
            assetInfoService.save(asset);
            success++;
        }
        return Result.success("成功导入 " + success + " 条资产");
    }

    /**
     * 根据 ID 查询资产详情
     */
    @GetMapping("/detail")
    public Result<AssetInfo> detail(@RequestParam Long assetId) {
        AssetInfo asset = assetInfoService.getById(assetId);
        if (asset == null) {
            return Result.fail("资产不存在");
        }
        return Result.success(asset);
    }

    /**
     * 更新资产
     */
    @PostMapping("/update")
    public Result<String> updateAsset(@RequestBody AssetInfo assetInfo) {
        if (assetInfo.getAssetId() == null) {
            return Result.error("资产 ID 不能为空");
        }
        // 记录变更历史
        AssetInfo old = assetInfoService.getById(assetInfo.getAssetId());
        if (old != null) {
            auditChange(old, assetInfo);
        }
        assetInfo.setUpdateTime(LocalDateTime.now());
        // 自动计算折旧和当前价值
        applyModelDefaults(assetInfo);
        calculateDepreciation(assetInfo);
        // 自动推算下次维护日期（对比旧值，判断是否需要重新推算）
        calculateMaintenance(old, assetInfo);
        boolean update = assetInfoService.updateById(assetInfo);
        if (update) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    // 审计变更
    private void auditChange(AssetInfo old, AssetInfo nu) {
        diff("assetName", old.getAssetName(), nu.getAssetName(), old);
        diff("assetCode", old.getAssetCode(), nu.getAssetCode(), old);
        diff("department", old.getDepartment(), nu.getDepartment(), old);
        diff("storageLocation", old.getStorageLocation(), nu.getStorageLocation(), old);
        diff("userName", old.getUserName(), nu.getUserName(), old);
        diff("status", old.getStatus() != null ? old.getStatus().toString() : null, nu.getStatus() != null ? nu.getStatus().toString() : null, old);
        diff("responsiblePerson", old.getResponsiblePerson(), nu.getResponsiblePerson(), old);
        diff("warrantyInfo", old.getWarrantyInfo(), nu.getWarrantyInfo(), old);
    }

    private void diff(String field, String o, String n, AssetInfo asset) {
        if (o == null && n == null) return;
        if (o != null && o.equals(n)) return;
        AssetChangeLog log = new AssetChangeLog();
        log.setAssetId(asset.getAssetId());
        log.setAssetCode(asset.getAssetCode());
        log.setFieldName(field);
        log.setOldValue(o);
        log.setNewValue(n);
        log.setOperator(asset.getUserName());
        changeLogMapper.insert(log);
    }

    @GetMapping("/change-log")
    public Result<?> changeLog(@RequestParam Long assetId) {
        return Result.success(changeLogMapper.selectList(
            new LambdaQueryWrapper<AssetChangeLog>().eq(AssetChangeLog::getAssetId, assetId).orderByDesc(AssetChangeLog::getChangeTime)));
    }

    /**
     * 删除资产
     */
    @PostMapping("/delete")
    public Result<String> deleteAsset(@RequestParam Long assetId) {
        boolean remove = assetInfoService.removeById(assetId);
        if (remove) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 根据分类查询资产
     */
    @GetMapping("/listByCategory")
    public Result<List<AssetInfo>> listByCategory(@RequestParam Long categoryId) {
        LambdaQueryWrapper<AssetInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetInfo::getCategoryId, categoryId);
        List<AssetInfo> list = assetInfoService.list(wrapper);
        return Result.success(list);
    }
    
    /**
     * 生成资产编号（格式：ZC+年月+序号）
     */
    private String generateAssetCode() {
        String prefix = "ZC" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        // 查询当天最大编号
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssetInfo> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.likeRight(AssetInfo::getAssetCode, prefix);
        wrapper.orderByDesc(AssetInfo::getAssetCode);
        wrapper.last("LIMIT 1");
        AssetInfo last = assetInfoService.getOne(wrapper, false);
        int seq = 1;
        if (last != null && last.getAssetCode() != null) {
            String lastCode = last.getAssetCode();
            if (lastCode.length() > prefix.length()) {
                try { seq = Integer.parseInt(lastCode.substring(prefix.length())) + 1; } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 批量更新资产字段
     */
    @PostMapping("/batchUpdate")
    public Result<String> batchUpdate(@RequestBody BatchUpdateDTO dto) {
        if (dto.getAssetIds() == null || dto.getAssetIds().isEmpty()) return Result.error("请选择至少一条资产");
        if (dto.getFields() == null || dto.getFields().isEmpty()) return Result.error("请设置要修改的字段");

        int count = 0;
        for (Long id : dto.getAssetIds()) {
            AssetInfo asset = assetInfoService.getById(id);
            if (asset == null) continue;

            if (dto.getFields().containsKey("department")) asset.setDepartment((String) dto.getFields().get("department"));
            if (dto.getFields().containsKey("storageLocation")) asset.setStorageLocation((String) dto.getFields().get("storageLocation"));
            if (dto.getFields().containsKey("userName")) asset.setUserName((String) dto.getFields().get("userName"));
            if (dto.getFields().containsKey("status")) asset.setStatus((Integer) dto.getFields().get("status"));
            if (dto.getFields().containsKey("categoryId")) asset.setCategoryId(Long.valueOf(dto.getFields().get("categoryId").toString()));
            if (dto.getFields().containsKey("responsiblePerson")) asset.setResponsiblePerson((String) dto.getFields().get("responsiblePerson"));
            asset.setUpdateTime(LocalDateTime.now());
            assetInfoService.updateById(asset);
            count++;
        }
        return Result.success("批量更新成功，共 " + count + " 条");
    }

    // ==================== P0: 折旧/模型 辅助方法 ====================

    /**
     * 如果选择了资产模型，从模型继承折旧参数
     */
    private void applyModelDefaults(AssetInfo asset) {
        if (asset.getModelId() == null) return;
        AssetModel model = assetModelService.getById(asset.getModelId());
        if (model == null) return;
        // 继承模型默认值（仅当资产自身未设置时）
        if (asset.getDepreciationYears() == null && model.getDepreciationYears() != null) {
            asset.setDepreciationYears(model.getDepreciationYears());
        }
        if (asset.getDepreciationMethod() == null && model.getDepreciationMethod() != null) {
            asset.setDepreciationMethod(model.getDepreciationMethod());
        }
        if (asset.getEolDate() == null && asset.getPurchaseDate() != null && model.getEolMonths() != null) {
            asset.setEolDate(asset.getPurchaseDate().plusMonths(model.getEolMonths()));
        }
        // 同步模型字段到资产的model文本
        if (asset.getModel() == null || asset.getModel().isEmpty()) {
            asset.setModel(model.getModelName());
        }
    }

    /**
     * 自动计算EOL日期、折旧率和当前价值
     * 直线折旧法：每月折旧 = 采购成本 / (折旧年限 * 12)
     */
    private void calculateDepreciation(AssetInfo asset) {
        // 采购成本默认取purchasePrice
        BigDecimal cost = asset.getPurchaseCost();
        if (cost == null && asset.getPurchasePrice() != null) {
            cost = asset.getPurchasePrice();
            asset.setPurchaseCost(cost);
        }
        if (cost == null) return;

        LocalDate purchaseDate = asset.getPurchaseDate();
        if (purchaseDate == null) return;

        // 折旧年限默认3年
        Integer depYears = asset.getDepreciationYears();
        if (depYears == null || depYears <= 0) {
            depYears = 3;
            asset.setDepreciationYears(depYears);
        }

        // 年折旧率
        if (asset.getDepreciationRate() == null) {
            BigDecimal rate = BigDecimal.valueOf(100.0).divide(BigDecimal.valueOf(depYears), 2, RoundingMode.HALF_UP);
            asset.setDepreciationRate(rate);
        }

        // EOL日期
        if (asset.getEolDate() == null) {
            asset.setEolDate(purchaseDate.plusMonths(depYears * 12L));
        }

        // 当前价值 = max(0, cost - cost * 已使用月数 / (depYears * 12))
        long monthsUsed = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
        if (monthsUsed < 0) monthsUsed = 0;
        long totalMonths = depYears * 12L;
        BigDecimal depreciationAmount = cost.multiply(BigDecimal.valueOf(monthsUsed))
                .divide(BigDecimal.valueOf(totalMonths), 2, RoundingMode.HALF_UP);
        BigDecimal currentValue = cost.subtract(depreciationAmount);
        if (currentValue.compareTo(BigDecimal.ZERO) < 0) {
            currentValue = BigDecimal.ZERO;
        }
        asset.setCurrentValue(currentValue);
    }

    /**
     * 自动推算下次维护日期
     * - 新增时：若 purchase_date 有值、next_maintenance_date 为空、maintenance_cycle_days 有值，则自动推算
     * - 更新时：若 maintenance_cycle_days 变了但 next_maintenance_date 未手动修改，则重新推算
     */
    private void calculateMaintenance(AssetInfo old, AssetInfo asset) {
        if (asset.getPurchaseDate() == null) {
            // purchase_date 为空时，保留手动设置的 next_maintenance_date（不覆盖）
            return;
        }
        if (old == null) {
            // 新增：首次自动推算
            if (asset.getNextMaintenanceDate() == null && asset.getMaintenanceCycleDays() != null) {
                asset.setNextMaintenanceDate(asset.getPurchaseDate().plusDays(asset.getMaintenanceCycleDays()));
            }
        } else {
            // 更新：判断 maintenance_cycle_days 是否改变
            boolean cycleChanged = !java.util.Objects.equals(old.getMaintenanceCycleDays(), asset.getMaintenanceCycleDays());
            boolean nextDateChanged = !java.util.Objects.equals(old.getNextMaintenanceDate(), asset.getNextMaintenanceDate());
            if (cycleChanged && !nextDateChanged && asset.getMaintenanceCycleDays() != null) {
                // 维护周期变了但下次维护日期未手动修改 → 重新推算
                asset.setNextMaintenanceDate(asset.getPurchaseDate().plusDays(asset.getMaintenanceCycleDays()));
            } else if (asset.getNextMaintenanceDate() == null && asset.getMaintenanceCycleDays() != null) {
                // 下次维护日期本身为空（之前也可能为空）→ 自动推算
                asset.setNextMaintenanceDate(asset.getPurchaseDate().plusDays(asset.getMaintenanceCycleDays()));
            }
        }
    }
}
