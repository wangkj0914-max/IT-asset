package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.dto.UseApplyDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetUseRecord;
import com.asset.itassetsystem.mapper.AssetUseRecordMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetUseService;
import com.asset.itassetsystem.vo.UseRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资产领用服务实现类
 */
@Service
public class AssetUseServiceImpl extends ServiceImpl<AssetUseRecordMapper, AssetUseRecord> implements AssetUseService {

    @Autowired
    private AssetInfoService assetInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(UseApplyDTO dto, String operator) {
        // 检查资产状态
        AssetInfo asset = assetInfoService.getById(dto.getAssetId());
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (asset.getStatus() == 1) {
            throw new RuntimeException("资产已被领用");
        }
        if (asset.getStatus() == 3) {
            throw new RuntimeException("资产已报废，无法领用");
        }

        // 创建领用记录
        AssetUseRecord record = new AssetUseRecord();
        record.setAssetId(dto.getAssetId());
        record.setUserId(1L); // 默认用户 ID，实际应从 token 中获取
        record.setDepartment(dto.getDepartment());
        record.setContactPerson(dto.getContactPerson());
        record.setUseType(1); // 领用
        record.setUseDate(LocalDateTime.now());
        record.setExpectedReturnDate(dto.getExpectedReturnDate());
        record.setApproveStatus(0); // 待审批
        record.setRemark(dto.getRemark());
        
        boolean saved = save(record);
        
        // 如果是普通员工领用，需要审批；管理员领用直接通过
        if (saved && "admin".equals(operator)) {
            approve(record.getRecordId(), true, operator);
        }
        
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long recordId, boolean approved, String approver) {
        AssetUseRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("领用记录不存在");
        }

        record.setApproveUser(approver);
        record.setApproveStatus(approved ? 1 : 2);
        record.setApproveTime(LocalDateTime.now());
        
        boolean updated = updateById(record);
        
        if (updated && approved) {
            // 更新资产状态为已领用
            AssetInfo asset = assetInfoService.getById(record.getAssetId());
            if (asset != null) {
                asset.setStatus(1); // 已领用
                assetInfoService.updateById(asset);
            }
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnAsset(Long assetId, String operator) {
        AssetInfo asset = assetInfoService.getById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (asset.getStatus() != 1) {
            throw new RuntimeException("资产未领用，无法归还");
        }

        // 查找该资产最近一条已通过的领用记录
        LambdaQueryWrapper<AssetUseRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetUseRecord::getAssetId, assetId)
                    .eq(AssetUseRecord::getUseType, 1)
                    .eq(AssetUseRecord::getApproveStatus, 1)
                    .orderByDesc(AssetUseRecord::getRecordId)
                    .last("LIMIT 1");
        AssetUseRecord borrowRecord = getOne(queryWrapper);
        if (borrowRecord == null) {
            throw new RuntimeException("未找到该资产的领用记录");
        }

        // 更新领用记录的归还信息
        LocalDateTime now = LocalDateTime.now();
        borrowRecord.setActualReturnDate(now);
        // 判断是否逾期
        if (borrowRecord.getExpectedReturnDate() != null && now.isAfter(borrowRecord.getExpectedReturnDate())) {
            borrowRecord.setOverdueStatus(1); // 已逾期
        } else {
            borrowRecord.setOverdueStatus(2); // 已关闭
        }
        updateById(borrowRecord);

        // 更新资产状态为未领用
        asset.setStatus(0);
        return assetInfoService.updateById(asset);
    }

    @Override
    public List<AssetUseRecord> listByAssetId(Long assetId) {
        LambdaQueryWrapper<AssetUseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetUseRecord::getAssetId, assetId);
        wrapper.orderByDesc(AssetUseRecord::getUseDate);
        return list(wrapper);
    }

    @Override
    public List<AssetUseRecord> listPending() {
        LambdaQueryWrapper<AssetUseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetUseRecord::getApproveStatus, 0);
        wrapper.orderByDesc(AssetUseRecord::getUseDate);
        return list(wrapper);
    }
    
    @Override
    public IPage<UseRecordVO> listAllWithAssetInfo(Long current, Long size, String assetName, Integer status, Integer overdue) {
        // 分页查询领用记录
        IPage<AssetUseRecord> recordPage = new Page<>(current, size);
        LambdaQueryWrapper<AssetUseRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(AssetUseRecord::getApproveStatus, status);
        }
        if (overdue != null) {
            wrapper.eq(AssetUseRecord::getOverdueStatus, overdue);
        }
        wrapper.orderByDesc(AssetUseRecord::getUseDate);
        
        IPage<AssetUseRecord> page = page(recordPage, wrapper);
        
        // 转换为 VO（包含资产名称）
        IPage<UseRecordVO> voPage = new Page<>(current, size);
        voPage.setTotal(page.getTotal());
        
        // 批量查询关联的资产信息，避免 N+1 问题
        Set<Long> assetIds = page.getRecords().stream()
                .map(AssetUseRecord::getAssetId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, AssetInfo> assetMap = assetIds.isEmpty()
                ? new HashMap<>()
                : assetInfoService.listByIds(new ArrayList<>(assetIds)).stream()
                    .collect(Collectors.toMap(AssetInfo::getAssetId, a -> a, (a1, a2) -> a1));

        List<UseRecordVO> voList = page.getRecords().stream().map(record -> {
            UseRecordVO vo = new UseRecordVO();
            vo.setRecordId(record.getRecordId());
            vo.setAssetId(record.getAssetId());
            vo.setUserId(record.getUserId());
            vo.setDepartment(record.getDepartment());
            vo.setContactPerson(record.getContactPerson());
            vo.setContactPhone(record.getContactPhone());
            vo.setUseType(record.getUseType());
            vo.setUseDate(record.getUseDate());
            vo.setExpectedReturnDate(record.getExpectedReturnDate());
            vo.setActualReturnDate(record.getActualReturnDate());
            vo.setOverdueStatus(record.getOverdueStatus());
            vo.setReturnDate(record.getReturnDate());
            vo.setApproveUser(record.getApproveUser());
            vo.setApproveStatus(record.getApproveStatus());
            vo.setApproveTime(record.getApproveTime());
            vo.setRemark(record.getRemark());
            
            // 从批量查询结果中获取资产信息
            if (record.getAssetId() != null) {
                AssetInfo asset = assetMap.get(record.getAssetId());
                if (asset != null) {
                    vo.setAssetName(asset.getAssetName());
                    vo.setAssetCode(asset.getAssetCode());
                    vo.setSite(asset.getSite());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        // 如果传入了资产名称筛选，过滤结果
        if (StringUtils.hasText(assetName)) {
            voList = voList.stream()
                .filter(vo -> vo.getAssetName() != null && vo.getAssetName().contains(assetName))
                .collect(Collectors.toList());
        }
        
        voPage.setRecords(voList);
        return voPage;
    }
}
