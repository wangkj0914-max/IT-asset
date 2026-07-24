package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.dto.RepairApplyDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetRepairRecord;
import com.asset.itassetsystem.mapper.AssetRepairRecordMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetRepairService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产维修服务实现类
 */
@Service
public class AssetRepairServiceImpl extends ServiceImpl<AssetRepairRecordMapper, AssetRepairRecord> implements AssetRepairService {

    @Autowired
    private AssetInfoService assetInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(RepairApplyDTO dto) {
        AssetInfo asset = assetInfoService.getById(dto.getAssetId());
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }

        AssetRepairRecord record = new AssetRepairRecord();
        record.setAssetId(dto.getAssetId());
        record.setRepairReason(dto.getRepairReason());
        record.setRepairStatus(0); // 待维修
        record.setApplyUserId(dto.getApplyUserId());
        record.setApplyUserName(dto.getApplyUserName());
        record.setApplyDepartment(dto.getApplyDepartment());
        record.setRemark(dto.getRemark());
        
        boolean saved = save(record);
        
        if (saved) {
            // 更新资产状态为维修中
            asset.setStatus(2);
            assetInfoService.updateById(asset);
        }
        
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long repairId, Integer status, String repairMan) {
        AssetRepairRecord record = getById(repairId);
        if (record == null) {
            throw new RuntimeException("维修记录不存在");
        }
        
        record.setRepairStatus(status);
        if (status == 1) {
            record.setRepairDate(LocalDateTime.now());
            if (repairMan != null && !repairMan.trim().isEmpty()) {
                record.setRepairMan(repairMan);
            }
        }
        return updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long repairId, BigDecimal cost, String remark) {
        AssetRepairRecord record = getById(repairId);
        if (record == null) {
            throw new RuntimeException("维修记录不存在");
        }
        
        record.setRepairStatus(2); // 已完成
        record.setRepairCost(cost);
        record.setRepairDate(LocalDateTime.now());
        record.setRemark(remark);
        
        boolean updated = updateById(record);
        
        if (updated) {
            // 更新资产状态为未领用（或保持原状态）
            AssetInfo asset = assetInfoService.getById(record.getAssetId());
            if (asset != null) {
                asset.setStatus(0); // 维修完成，设为未领用
                assetInfoService.updateById(asset);
            }
        }
        
        return updated;
    }

    @Override
    public List<AssetRepairRecord> listByAssetId(Long assetId) {
        return lambdaQuery()
            .eq(AssetRepairRecord::getAssetId, assetId)
            .orderByDesc(AssetRepairRecord::getCreateTime)
            .list();
    }
}
