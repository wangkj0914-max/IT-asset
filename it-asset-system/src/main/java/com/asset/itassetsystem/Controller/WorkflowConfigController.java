package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.WorkflowConfig;
import com.asset.itassetsystem.service.WorkflowConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程设置 — 配置各模块审批开关
 */
@RestController
@RequestMapping("/workflow-config")
public class WorkflowConfigController {

    @Autowired private WorkflowConfigService workflowConfigService;

    @GetMapping("/list")
    public Result<List<WorkflowConfig>> list() {
        List<WorkflowConfig> list = workflowConfigService.list();
        return Result.success(list);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody WorkflowConfig config) {
        if (config.getId() == null) return Result.fail("缺少ID");
        workflowConfigService.updateById(config);
        return Result.success("ok");
    }
}
