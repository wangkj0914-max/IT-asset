package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.dto.InventoryCreateDTO;
import com.asset.itassetsystem.dto.InventoryReportDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetInventory;
import com.asset.itassetsystem.entity.AssetInventoryDetail;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.mapper.AssetInventoryDetailMapper;
import com.asset.itassetsystem.mapper.AssetInventoryMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetInventoryService;
import com.asset.itassetsystem.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 资产盘点服务实现类
 * 支持多维度筛选：部门、资产类别、存放地点、资产状态等
 */
@Slf4j
@Service
public class AssetInventoryServiceImpl extends ServiceImpl<AssetInventoryMapper, AssetInventory> implements AssetInventoryService {
    
    @Autowired
    private AssetInventoryMapper inventoryMapper;
    
    @Autowired
    private AssetInventoryDetailMapper detailMapper;
    
    @Autowired
    private AssetInfoService assetInfoService;
    
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private HttpServletRequest request;
    
    /**
     * 盘点范围类型常量
     */
    private static final int RANGE_ALL = 0;           // 全部资产
    private static final int RANGE_BY_CATEGORY = 1;   // 按资产分类筛选
    private static final int RANGE_BY_LOCATION = 2;   // 按存放地点筛选
    private static final int RANGE_BY_STATUS = 3;     // 按资产状态筛选
    private static final int RANGE_BY_USER = 4;       // 按使用人筛选
    private static final int RANGE_COMBINED = 5;      // 组合条件筛选
    private static final int RANGE_SPECIFIED = 9;     // 指定资产列表
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInventory(InventoryCreateDTO dto) {
        log.info("开始创建盘点任务，名称：{}, 范围类型：{}", dto.getInventoryName(), dto.getInventoryRange());
        
        // 1. 生成盘点单号（格式：PDyyyyMMdd + 4 位随机数）
        String inventoryNo = generateInventoryNo();
        log.info("生成盘点单号：{}", inventoryNo);
        
        // 2. 获取当前用户
        SysUser currentUser = sysUserService.getById(1L);
        
        // 3. 创建盘点任务主记录
        AssetInventory inventory = new AssetInventory();
        inventory.setInventoryNo(inventoryNo);
        inventory.setInventoryName(dto.getInventoryName());
        inventory.setInventoryRange(dto.getInventoryRange());
        inventory.setRangeValue(buildRangeDescription(dto));
        inventory.setInventoryDate(LocalDateTime.now());
        inventory.setOperatorId(currentUser.getUserId());
        inventory.setOperatorName(currentUser.getRealName());
        inventory.setStatus(0); // 待盘点
        inventory.setRemark(dto.getRemark());
        
        // 从请求上下文获取当前站点
        try {
            var attrs = org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                String s = ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest().getParameter("site");
                if (s != null && !s.isEmpty()) inventory.setSite(s);
            }
        } catch (Exception ignored) {}
        
        inventoryMapper.insert(inventory);
        log.info("盘点任务主记录创建成功，ID: {}", inventory.getInventoryId());
        
        // 4. 根据筛选条件获取资产列表，生成盘点明细
        List<AssetInfo> assets = getAssetsByFilter(dto);
        log.info("筛选到资产数量：{}", assets.size());
        
        if (CollectionUtils.isEmpty(assets)) {
            log.warn("未找到符合条件的资产，盘点任务创建完成但无明细");
            return;
        }
        
        // 5. 批量生成盘点明细
        List<AssetInventoryDetail> details = buildInventoryDetails(inventory.getInventoryId(), assets);
        
        // 6. 批量插入明细记录
        int insertCount = 0;
        for (AssetInventoryDetail detail : details) {
            detailMapper.insert(detail);
            insertCount++;
        }
        
        log.info("盘点明细创建成功，共 {} 条", insertCount);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInventoryStatus(Long inventoryId, Integer status) {
        AssetInventory inventory = inventoryMapper.selectById(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("盘点任务不存在");
        }
        
        inventory.setStatus(status);
        inventoryMapper.updateById(inventory);
        log.info("盘点任务状态更新成功，inventoryId: {}, status: {}", inventoryId, status);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInventory(Long detailId, Integer status, String remark, String actualLocation, String differenceType) {
        AssetInventoryDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("盘点明细不存在");
        }

        // 从请求上下文获取当前用户名
        String checker = (String) request.getAttribute("username");
        if (checker == null || checker.isEmpty()) {
            checker = request.getHeader("X-Username");
        }
        if (checker == null || checker.isEmpty()) {
            checker = "系统"; // 兜底
        }

        // 更新盘点状态和结果
        detail.setStatus(status); // 1-正常 2-盘盈 3-盘亏
        detail.setResultRemark(remark);
        detail.setCheckTime(LocalDateTime.now());
        detail.setCheckerName(checker);
        detail.setScannedAt(LocalDateTime.now());
        detail.setActualLocation(actualLocation);
        detail.setVerifiedBy(checker);

        // 判断差异类型：如果前端传了则使用前端值，否则根据资产原始值判断
        if (differenceType != null && !differenceType.isEmpty()) {
            detail.setDifferenceType(differenceType);
        } else if (detail.getAssetId() != null) {
            // 根据资产原始数据自动判断
            AssetInfo asset = assetInfoService.getById(detail.getAssetId());
            if (asset != null) {
                if (status == 3) {
                    detail.setDifferenceType("MISSING");
                } else if (status == 2) {
                    detail.setDifferenceType("EXTRA");
                } else {
                    // 正常状态，检查位置和资产状态是否匹配
                    boolean locationDiffer = actualLocation != null && !actualLocation.isEmpty()
                            && !actualLocation.equals(asset.getStorageLocation());
                    // 资产系统状态异常（如已报废）但盘点正常 → 状态不符
                    boolean statusDiffer = asset.getStatus() != null && asset.getStatus() == 3;
                    if (locationDiffer) {
                        detail.setDifferenceType("LOCATION");
                    } else if (statusDiffer) {
                        detail.setDifferenceType("STATUS");
                    } else {
                        detail.setDifferenceType("NONE");
                    }
                }
            } else {
                detail.setDifferenceType("NONE");
            }
        } else {
            detail.setDifferenceType("NONE");
        }

        detailMapper.updateById(detail);
        log.info("盘点明细更新成功，detailId: {}, status: {}, checker: {}, differenceType: {}",
                detailId, status, checker, detail.getDifferenceType());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishInventory(Long inventoryId) {
        AssetInventory inventory = inventoryMapper.selectById(inventoryId);
        if (inventory == null) {
            throw new RuntimeException("盘点任务不存在");
        }
        
        // 查询所有明细，统计盘盈盘亏数量
        LambdaQueryWrapper<AssetInventoryDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetInventoryDetail::getInventoryId, inventoryId);
        List<AssetInventoryDetail> details = detailMapper.selectList(wrapper);
        
        int surplusCount = 0; // 盘盈
        int lossCount = 0;    // 盘亏
        int normalCount = 0;  // 正常
        
        for (AssetInventoryDetail detail : details) {
            if (detail.getStatus() == 1) {
                normalCount++;
            } else if (detail.getStatus() == 2) {
                surplusCount++;
            } else if (detail.getStatus() == 3) {
                lossCount++;
            }
        }
        
        // 更新盘点任务状态为已完成
        inventory.setSurplusCount(surplusCount);
        inventory.setLossCount(lossCount);
        inventory.setStatus(2);
        
        inventoryMapper.updateById(inventory);
        log.info("盘点任务完成，总数：{}, 正常：{}, 盘盈：{}, 盘亏：{}", 
                details.size(), normalCount, surplusCount, lossCount);
    }

    @Override
    public InventoryReportDTO generateReport(Long inventoryId) {
        InventoryReportDTO report = new InventoryReportDTO();

        LambdaQueryWrapper<AssetInventoryDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetInventoryDetail::getInventoryId, inventoryId);
        List<AssetInventoryDetail> allDetails = detailMapper.selectList(wrapper);

        int normalCount = 0, surplusCount = 0, lossCount = 0;
        int locationDiffCount = 0, statusDiffCount = 0, missingCount = 0, extraCount = 0;
        List<AssetInventoryDetail> diffDetails = new ArrayList<>();

        for (AssetInventoryDetail detail : allDetails) {
            if (detail.getStatus() == 1) {
                normalCount++;
                // 正常状态也可能有位置差异
                if ("LOCATION".equals(detail.getDifferenceType())) {
                    locationDiffCount++;
                    diffDetails.add(detail);
                } else if ("STATUS".equals(detail.getDifferenceType())) {
                    statusDiffCount++;
                    diffDetails.add(detail);
                }
            } else if (detail.getStatus() == 2) {
                surplusCount++;
                extraCount++;
                diffDetails.add(detail);
            } else if (detail.getStatus() == 3) {
                lossCount++;
                missingCount++;
                diffDetails.add(detail);
            }

            // 单独统计差异类型
            if ("LOCATION".equals(detail.getDifferenceType())) {
                locationDiffCount++;
            } else if ("STATUS".equals(detail.getDifferenceType())) {
                statusDiffCount++;
            }
        }

        report.setNormalCount(normalCount);
        report.setSurplusCount(surplusCount);
        report.setLossCount(lossCount);
        report.setLocationDiffCount(locationDiffCount);
        report.setStatusDiffCount(statusDiffCount);
        report.setMissingCount(missingCount);
        report.setExtraCount(extraCount);
        report.setTotalCount(allDetails.size());
        report.setDetails(diffDetails);

        log.info("生成盘点报告，inventoryId: {}, 正常: {}, 盘盈: {}, 盘亏: {}, 位置不符: {}, 状态不符: {}",
                inventoryId, normalCount, surplusCount, lossCount, locationDiffCount, statusDiffCount);

        return report;
    }
    
    @Override
    public AssetInventory getDetail(Long inventoryId) {
        return inventoryMapper.selectById(inventoryId);
    }
    
    @Override
    public List<AssetInventoryDetail> listDetails(Long inventoryId) {
        LambdaQueryWrapper<AssetInventoryDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetInventoryDetail::getInventoryId, inventoryId);
        return detailMapper.selectList(wrapper);
    }
    
    @Override
    public Object listPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<AssetInventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetInventory> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(AssetInventory::getStatus, status);
        }
        
        wrapper.orderByDesc(AssetInventory::getCreateTime);
        
        Page<AssetInventory> resultPage = inventoryMapper.selectPage(page, wrapper);
        
        return new PageResult<>(
            resultPage.getRecords(),
            resultPage.getTotal(),
            resultPage.getCurrent(),
            resultPage.getSize()
        );
    }
    
    /**
     * 根据筛选条件获取资产列表
     * 支持多维度组合筛选
     */
    private List<AssetInfo> getAssetsByFilter(InventoryCreateDTO dto) {
        LambdaQueryWrapper<AssetInfo> wrapper = new LambdaQueryWrapper<>();
        
        switch (dto.getInventoryRange()) {
            case RANGE_ALL:
                // 全部资产（排除已报废）
                wrapper.ne(AssetInfo::getStatus, 3);
                break;
                
            case RANGE_BY_CATEGORY:
                // 按资产分类筛选
                if (!CollectionUtils.isEmpty(dto.getCategoryIds())) {
                    wrapper.in(AssetInfo::getCategoryId, dto.getCategoryIds());
                }
                break;
                
            case RANGE_BY_LOCATION:
                // 按存放地点筛选
                if (!CollectionUtils.isEmpty(dto.getStorageLocations())) {
                    wrapper.in(AssetInfo::getStorageLocation, dto.getStorageLocations());
                }
                break;
                
            case RANGE_BY_STATUS:
                // 按资产状态筛选
                if (!CollectionUtils.isEmpty(dto.getAssetStatuses())) {
                    wrapper.in(AssetInfo::getStatus, dto.getAssetStatuses());
                }
                break;
                
            case RANGE_BY_USER:
                // 按使用人筛选
                if (!CollectionUtils.isEmpty(dto.getUserIds())) {
                    wrapper.in(AssetInfo::getUserId, dto.getUserIds());
                }
                break;
                
            case RANGE_COMBINED:
                // 组合条件筛选
                buildCombinedFilter(wrapper, dto);
                break;
                
            case RANGE_SPECIFIED:
                // 指定资产列表
                if (!CollectionUtils.isEmpty(dto.getAssetIds())) {
                    wrapper.in(AssetInfo::getAssetId, dto.getAssetIds());
                }
                break;
                
            default:
                wrapper.ne(AssetInfo::getStatus, 3);
        }
        
        return assetInfoService.list(wrapper);
    }
    
    /**
     * 构建组合筛选条件
     */
    private void buildCombinedFilter(LambdaQueryWrapper<AssetInfo> wrapper, InventoryCreateDTO dto) {
        boolean hasCondition = false;
        
        // 分类筛选
        if (!CollectionUtils.isEmpty(dto.getCategoryIds())) {
            wrapper.in(AssetInfo::getCategoryId, dto.getCategoryIds());
            hasCondition = true;
        }
        
        // 存放地点筛选
        if (!CollectionUtils.isEmpty(dto.getStorageLocations())) {
            wrapper.in(AssetInfo::getStorageLocation, dto.getStorageLocations());
            hasCondition = true;
        }
        
        // 资产状态筛选
        if (!CollectionUtils.isEmpty(dto.getAssetStatuses())) {
            wrapper.in(AssetInfo::getStatus, dto.getAssetStatuses());
            hasCondition = true;
        }
        
        // 使用人筛选
        if (!CollectionUtils.isEmpty(dto.getUserIds())) {
            wrapper.in(AssetInfo::getUserId, dto.getUserIds());
            hasCondition = true;
        }
        
        // 如果没有指定任何条件，默认排除已报废资产
        if (!hasCondition) {
            wrapper.ne(AssetInfo::getStatus, 3);
        }
        // 如果指定了状态但不包含已报废，则排除已报废
        else if (CollectionUtils.isEmpty(dto.getAssetStatuses()) || !dto.getAssetStatuses().contains(3)) {
            wrapper.ne(AssetInfo::getStatus, 3);
        }
    }
    
    /**
     * 批量构建盘点明细
     */
    private List<AssetInventoryDetail> buildInventoryDetails(Long inventoryId, List<AssetInfo> assets) {
        List<AssetInventoryDetail> details = new ArrayList<>(assets.size());
        
        for (AssetInfo asset : assets) {
            AssetInventoryDetail detail = new AssetInventoryDetail();
            detail.setInventoryId(inventoryId);
            detail.setAssetId(asset.getAssetId());
            detail.setAssetCode(asset.getAssetCode());
            detail.setAssetName(asset.getAssetName());
            detail.setStorageLocation(asset.getStorageLocation());
            detail.setStatus(0); // 待盘点
            details.add(detail);
        }
        
        return details;
    }
    
    /**
     * 构建筛选范围描述（用于存储到 rangeValue 字段）
     */
    private String buildRangeDescription(InventoryCreateDTO dto) {
        StringBuilder desc = new StringBuilder();
        
        switch (dto.getInventoryRange()) {
            case RANGE_ALL:
                desc.append("全部资产");
                break;
            case RANGE_BY_CATEGORY:
                desc.append("按资产分类筛选");
                break;
            case RANGE_BY_LOCATION:
                desc.append("按存放地点筛选");
                break;
            case RANGE_BY_STATUS:
                desc.append("按资产状态筛选");
                break;
            case RANGE_BY_USER:
                desc.append("按使用人筛选");
                break;
            case RANGE_COMBINED:
                desc.append("组合条件筛选");
                break;
            case RANGE_SPECIFIED:
                desc.append("指定资产列表");
                break;
        }
        
        return desc.toString();
    }
    
    /**
     * 生成盘点单号
     * 格式：PD + yyyyMMdd + 4 位随机数
     */
    private String generateInventoryNo() {
        String datePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PD" + datePart + String.format("%04d", System.nanoTime() % 10000);
    }
    
    /**
     * 分页结果封装类
     */
    @Data
    public static class PageResult<T> {
        private List<T> records;
        private Long total;
        private Long current;
        private Long size;
        
        public PageResult(List<T> records, Long total, Long current, Long size) {
            this.records = records;
            this.total = total;
            this.current = current;
            this.size = size;
        }
    }
}
