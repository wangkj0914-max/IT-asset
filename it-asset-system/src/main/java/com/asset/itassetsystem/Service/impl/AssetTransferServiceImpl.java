package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.dto.TransferApplyDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetTransferRecord;
import com.asset.itassetsystem.mapper.AssetTransferRecordMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetTransferService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 资产调拨服务实现
 */
@Service
public class AssetTransferServiceImpl extends ServiceImpl<AssetTransferRecordMapper, AssetTransferRecord> implements AssetTransferService {

    @Autowired
    private AssetInfoService assetInfoService;

    @Override
    @Transactional
    public void apply(TransferApplyDTO dto) {
        AssetInfo asset = assetInfoService.getById(dto.getAssetId());
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        
        AssetTransferRecord record = new AssetTransferRecord();
        // 生成调拨单号：DB + 年月日 + 4位随机
        String no = "DB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000));
        record.setTransferNo(no);
        record.setAssetId(asset.getAssetId());
        record.setAssetName(asset.getAssetName());
        record.setAssetCode(asset.getAssetCode());
        record.setFromDepartment(asset.getDepartment());
        record.setFromLocation(asset.getStorageLocation());
        record.setFromUser(asset.getUserName());
        record.setToDepartment(dto.getToDepartment());
        record.setToLocation(dto.getToLocation());
        record.setToUser(dto.getToUser());
        record.setTransferReason(dto.getTransferReason());
        record.setTransferStatus(0); // 待审批
        record.setCreateTime(LocalDateTime.now());

        save(record);
    }

    @Override
    @Transactional
    public void approve(Long transferId, Integer status, String remark) {
        AssetTransferRecord record = getById(transferId);
        if (record == null) {
            throw new RuntimeException("调拨记录不存在");
        }
        if (record.getTransferStatus() != 0) {
            throw new RuntimeException("该调拨记录已处理");
        }

        record.setTransferStatus(status);
        record.setApproveRemark(remark);
        record.setApproveTime(LocalDateTime.now());
        updateById(record);

        // 审批通过：更新资产的归属信息
        if (status == 1) {
            AssetInfo asset = assetInfoService.getById(record.getAssetId());
            if (asset != null) {
                asset.setDepartment(record.getToDepartment());
                asset.setStorageLocation(record.getToLocation());
                asset.setUserName(record.getToUser());
                asset.setUpdateTime(LocalDateTime.now());
                assetInfoService.updateById(asset);
            }
        }
    }

    @Override
    public void delete(Long transferId) {
        AssetTransferRecord record = getById(transferId);
        if (record == null) {
            throw new RuntimeException("调拨记录不存在");
        }
        if (record.getTransferStatus() != 2) {
            throw new RuntimeException("只能删除已拒绝的调拨记录");
        }
        removeById(transferId);
    }
}
