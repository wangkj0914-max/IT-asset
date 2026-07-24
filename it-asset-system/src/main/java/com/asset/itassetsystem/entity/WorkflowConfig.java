package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_workflow_config")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class WorkflowConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String module;
    private String moduleName;
    private Integer requireApproval;
    private Integer autoApprove;
    private Integer approvalLevels;
    private String level1Approver;
    private String level2Approver;
    private String level3Approver;
    private String conditionField;
    private String conditionValue;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
