package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.AssetInbound;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.mapper.AssetInboundMapper;
import com.asset.itassetsystem.service.AssetInboundService;
import com.asset.itassetsystem.service.AssetInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 资产入库服务实现类
 */
@Service
public class AssetInboundServiceImpl extends ServiceImpl<AssetInboundMapper, AssetInbound> implements AssetInboundService {

    /**
     * 生成资产编号
     * 格式：ZC + 年月日 + 4 位序号
     * 示例：ZC202603180001
     */
    private synchronized String generateAssetCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查询今天已生成的最大资产编号
        LambdaQueryWrapper<AssetInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(AssetInfo::getAssetCode, "ZC" + datePart);
        wrapper.orderByDesc(AssetInfo::getAssetCode);
        
        AssetInfo lastAsset = assetInfoService.getOne(wrapper);
        
        if (lastAsset != null && lastAsset.getAssetCode() != null) {
            // 提取序号并 +1
            String lastCode = lastAsset.getAssetCode();
            try {
                int seq = Integer.parseInt(lastCode.substring(lastCode.length() - 4));
                seq++;
                return "ZC" + datePart + String.format("%04d", seq);
            } catch (Exception e) {
                // 解析失败，从 0001 开始
            }
        }
        
        // 第一个资产
        return "ZC" + datePart + "0001";
    }

    @Autowired
    private AssetInfoService assetInfoService;

    @Override
    public IPage<AssetInbound> pageInbounds(Long current, Long size, String assetName, String inboundNo, Integer status) {
        IPage<AssetInbound> page = new Page<>(current, size);
        LambdaQueryWrapper<AssetInbound> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(assetName)) {
            wrapper.like(AssetInbound::getAssetName, assetName);
        }
        if (StringUtils.hasText(inboundNo)) {
            wrapper.like(AssetInbound::getInboundNo, inboundNo);
        }
        if (status != null) {
            wrapper.eq(AssetInbound::getStatus, status);
        }
        
        wrapper.orderByDesc(AssetInbound::getApplyTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(AssetInbound inbound, String applicant) {
        // 生成入库单号
        String inboundNo = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                          (int)(Math.random() * 1000);
        
        inbound.setInboundNo(inboundNo);
        inbound.setStatus(0); // 待审核
        inbound.setApplicant(applicant);
        inbound.setApplyTime(LocalDateTime.now());
        
        return save(inbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long inboundId, boolean approved, String auditor) {
        AssetInbound inbound = getById(inboundId);
        if (inbound == null) {
            throw new RuntimeException("入库记录不存在");
        }
        
        inbound.setAuditor(auditor);
        inbound.setAuditTime(LocalDateTime.now());
        
        if (approved) {
            // 生成资产编号
            String assetCode = generateAssetCode();
            
            // 创建资产记录
            AssetInfo asset = new AssetInfo();
            asset.setAssetCode(assetCode); // 自动生成资产编号
            asset.setAssetName(inbound.getAssetName());
            asset.setCategoryId(inbound.getCategoryId());
            asset.setBrand(inbound.getBrand());
            asset.setModel(inbound.getModel());
            asset.setSerialNumber(inbound.getSerialNumber());
            asset.setPurchasePrice(inbound.getPurchasePrice());
            asset.setStorageLocation(inbound.getStorageLocation());
            asset.setStatus(0); // 未领用
            asset.setRemark(inbound.getRemark());
            
            assetInfoService.save(asset);
            
            // 关联资产 ID
            inbound.setAssetId(asset.getAssetId());
            inbound.setStatus(1); // 已入库
        } else {
            inbound.setStatus(2); // 已拒绝
        }
        
        return updateById(inbound);
    }
}
