package com.asset.itassetsystem.service;

import com.asset.itassetsystem.dto.UseApplyDTO;
import com.asset.itassetsystem.entity.AssetUseRecord;
import com.asset.itassetsystem.vo.UseRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 资产领用服务接口
 */
public interface AssetUseService extends IService<AssetUseRecord> {
    
    /**
     * 领用申请
     */
    boolean apply(UseApplyDTO dto, String operator);
    
    /**
     * 审批领用
     */
    boolean approve(Long recordId, boolean approved, String approver);
    
    /**
     * 资产归还
     */
    boolean returnAsset(Long assetId, String operator);
    
    /**
     * 查询资产的领用记录
     */
    List<AssetUseRecord> listByAssetId(Long assetId);
    
    /**
     * 查询待审批记录
     */
    List<AssetUseRecord> listPending();
    
    /**
     * 分页查询领用记录（包含资产名称）
     */
    IPage<UseRecordVO> listAllWithAssetInfo(Long current, Long size, String assetName, Integer status, Integer overdue);
}
