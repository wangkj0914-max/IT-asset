package com.asset.itassetsystem.dto;

import lombok.Data;

/**
 * 资产调拨申请 DTO
 */
@Data
public class TransferApplyDTO {
    private Long assetId;
    private String toDepartment;
    private String toLocation;
    private String toUser;
    private String transferReason;
}
