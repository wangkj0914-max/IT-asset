package com.asset.itassetsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 领用申请 DTO
 */
@Data
public class UseApplyDTO {
    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;
    
    @NotBlank(message = "领用部门不能为空")
    private String department;
    
    @NotBlank(message = "联系人不能为空")
    private String contactPerson;
    
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    
    private String remark;
}
