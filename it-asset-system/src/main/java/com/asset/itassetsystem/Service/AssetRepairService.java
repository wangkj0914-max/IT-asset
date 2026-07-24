package com.asset.itassetsystem.service;

import com.asset.itassetsystem.dto.RepairApplyDTO;
import com.asset.itassetsystem.entity.AssetRepairRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产维修服务接口
 */
public interface AssetRepairService extends IService<AssetRepairRecord> {
    
    /**
     * 报修申请
     */
    boolean apply(RepairApplyDTO dto);
    
    /**
     * 更新维修状态
     */
    boolean updateStatus(Long repairId, Integer status, String repairMan);
    
    /**
     * 完成维修
     */
    boolean complete(Long repairId, BigDecimal cost, String remark);
    
    /**
     * 查询资产的维修记录
     */
    List<AssetRepairRecord> listByAssetId(Long assetId);
}
