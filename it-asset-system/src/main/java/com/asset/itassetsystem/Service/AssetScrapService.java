package com.asset.itassetsystem.service;

import com.asset.itassetsystem.dto.ScrapApplyDTO;
import com.asset.itassetsystem.entity.AssetScrapRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 资产报废服务接口
 */
public interface AssetScrapService extends IService<AssetScrapRecord> {
    
    /**
     * 报废申请
     */
    boolean apply(ScrapApplyDTO dto, String operator);
    
    /**
     * 审批报废
     */
    boolean approve(Long scrapId, boolean approved, String approver);
    
    /**
     * 查询资产的报废记录
     */
    List<AssetScrapRecord> listByAssetId(Long assetId);
    
    /**
     * 查询待审批记录
     */
    List<AssetScrapRecord> listPending();
    
    /**
     * 分页查询报废记录
     */
    Object listPage(Integer pageNum, Integer pageSize, Integer scrapType, Integer approveStatus);
}
