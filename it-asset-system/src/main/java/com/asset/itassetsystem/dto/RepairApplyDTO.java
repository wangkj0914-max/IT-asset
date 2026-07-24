package com.asset.itassetsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 维修申请 DTO
 */
@Data
public class RepairApplyDTO {
    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;
    
    @NotBlank(message = "维修原因不能为空")
    private String repairReason;
    
    private Long applyUserId;
    private String applyUserName;
    private String applyDepartment;
    private String remark;
}
