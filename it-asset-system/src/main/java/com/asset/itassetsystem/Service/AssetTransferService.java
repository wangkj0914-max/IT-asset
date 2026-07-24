package com.asset.itassetsystem.service;

import com.asset.itassetsystem.dto.TransferApplyDTO;
import com.asset.itassetsystem.entity.AssetTransferRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 资产调拨服务接口
 */
public interface AssetTransferService extends IService<AssetTransferRecord> {
    void apply(TransferApplyDTO dto);
    void approve(Long transferId, Integer status, String remark);
    void delete(Long transferId);
}
