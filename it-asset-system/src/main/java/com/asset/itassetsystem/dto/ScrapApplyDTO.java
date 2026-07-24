package com.asset.itassetsystem.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 报废申请 DTO
 */
@Data
public class ScrapApplyDTO {
    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;
    
    @NotBlank(message = "报废原因不能为空")
    private String scrapReason;
    
    @NotNull(message = "报废类型不能为空")
    private Integer scrapType; // 0-正常报废 1-损坏报废 2-丢失报废
    
    private BigDecimal originalPrice;
    private BigDecimal residualValue;
    private String remark;
}
