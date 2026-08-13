package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysContract;
import com.asset.itassetsystem.service.SysContractService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/contract")
public class SysContractController {

    @Autowired
    private SysContractService sysContractService;

    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String site) {
        var wrapper = new LambdaQueryWrapper<SysContract>()
            .orderByDesc(SysContract::getCreateTime);
        if (site != null && !site.isEmpty()) {
            wrapper.eq(SysContract::getSite, site);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysContract::getContractName, keyword)
                .or().like(SysContract::getContractNo, keyword)
                .or().like(SysContract::getSupplier, keyword));
        }
        var page = sysContractService.page(new Page<>(current, size), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return Result.success(data);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody SysContract contract) {
        if (contract.getContractName() == null || contract.getContractName().isEmpty()) {
            return Result.fail("合同名称不能为空");
        }
        sysContractService.saveOrUpdate(contract);
        return Result.success("保存成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long contractId) {
        sysContractService.removeById(contractId);
        return Result.success("删除成功");
    }
}
