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

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    /**
     * 导出操作日志为CSV
     */
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws Exception {

        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) wrapper.eq(SysOperationLog::getModule, module);
        if (StringUtils.hasText(userName)) wrapper.like(SysOperationLog::getUserName, userName);
        if (status != null) wrapper.eq(SysOperationLog::getStatus, status);
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        wrapper.last("LIMIT 10000");

        List<SysOperationLog> list = logService.list(wrapper);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("ID,操作人,模块,操作,请求路径,请求参数,状态,错误信息,IP,耗时(ms),时间\n");
        for (SysOperationLog log : list) {
            sb.append(log.getLogId()).append(",")
              .append(csvEscape(log.getUserName())).append(",")
              .append(csvEscape(log.getModule())).append(",")
              .append(csvEscape(log.getOperation())).append(",")
              .append(csvEscape(log.getRequestUri())).append(",")
              .append(csvEscape(log.getRequestParams())).append(",")
              .append(log.getStatus() == 1 ? "成功" : "失败").append(",")
              .append(csvEscape(log.getErrorMsg())).append(",")
              .append(csvEscape(log.getIp())).append(",")
              .append(log.getCostTime()).append(",")
              .append(log.getCreateTime() != null ? log.getCreateTime().format(fmt) : "").append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=操作日志_" +
            java.time.LocalDate.now() + ".csv");
        response.setContentLength(bytes.length);
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
    }

    private String csvEscape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
