package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.CommonUtil;
import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetInbound;
import com.asset.itassetsystem.service.AssetInboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 资产入库控制器
 */
@RestController
@RequestMapping("/inbound")
public class AssetInboundController {

    @Autowired
    private AssetInboundService assetInboundService;
    @Autowired
    private HttpServletRequest httpRequest;

    private String getCurrentUser() {
        Object u = httpRequest.getAttribute("username");
        return u != null ? u.toString() : "system";
    }

    /**
     * 分页查询入库记录
     */
    @GetMapping({"/list-all", "/page"})
    public Result<Map<String, Object>> listAll(
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String inboundNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        var page = assetInboundService.pageInbounds(current, size, assetName, inboundNo, status);
        if (site != null && !site.isEmpty() && page.getRecords() != null) {
            var filtered = page.getRecords().stream()
                .filter(r -> site.equals(r.getSite()))
                .collect(java.util.stream.Collectors.toList());
            page.setRecords(filtered);
            page.setTotal((long) filtered.size());
        }
        return Result.success(CommonUtil.buildPageResult(page));
    }

    /**
     * 提交入库申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody AssetInbound inbound) {
        try {
            assetInboundService.apply(inbound, getCurrentUser());
            return Result.success("入库申请提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核入库
     */
    @PostMapping("/audit")
    public Result<String> audit(
            @RequestParam Long inboundId,
            @RequestParam Boolean approved) {
        try {
            assetInboundService.audit(inboundId, approved, getCurrentUser());
            return Result.success(approved ? "审核通过，资产已入库" : "已拒绝");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
