package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysOperationLog;
import com.asset.itassetsystem.service.SysOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志查询控制器
 */
@RestController
@RequestMapping("/log")
public class OperationLogController {

    @Autowired
    private SysOperationLogService logService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<IPage<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status) {

        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(module)) wrapper.eq(SysOperationLog::getModule, module);
        if (StringUtils.hasText(userName)) wrapper.like(SysOperationLog::getUserName, userName);
        if (status != null) wrapper.eq(SysOperationLog::getStatus, status);

        wrapper.orderByDesc(SysOperationLog::getCreateTime);

        return Result.success(logService.page(page, wrapper));
    }
}
