package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.AssetInbound;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 资产入库服务接口
 */
public interface AssetInboundService extends IService<AssetInbound> {
    
    /**
     * 分页查询入库记录
     */
    IPage<AssetInbound> pageInbounds(Long current, Long size, String assetName, String inboundNo, Integer status);
    
    /**
     * 提交入库申请
     */
    boolean apply(AssetInbound inbound, String applicant);
    
    /**
     * 审核入库
     */
    boolean audit(Long inboundId, boolean approved, String auditor);
}
