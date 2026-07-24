package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.*;
import com.asset.itassetsystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 统一审批中心 — 聚合所有待审批项
 */
@RestController
@RequestMapping("/approval")
public class ApprovalCenterController {

    @Autowired private AssetInboundService assetInboundService;
    @Autowired private AssetUseService assetUseService;
    @Autowired private AssetScrapService assetScrapService;
    @Autowired private AssetTransferService assetTransferService;
    @Autowired private AssetReturnRecordService assetReturnRecordService;
    @Autowired private ConsumableUseRecordService consumableUseRecordService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/pending")
    public Result<Map<String, Object>> getPending(@RequestParam(required = false) String site) {
        String currentSite = site != null ? site : getCurrentSite();

        Map<String, Object> result = new LinkedHashMap<>();

        // 入库待审核（status=0）
        LambdaQueryWrapper<AssetInbound> inboundW = new LambdaQueryWrapper<>();
        inboundW.eq(AssetInbound::getStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) {
            inboundW.eq(AssetInbound::getSite, currentSite);
        }
        inboundW.orderByDesc(AssetInbound::getApplyTime);
        List<Map<String, Object>> inbounds = new ArrayList<>();
        for (AssetInbound a : assetInboundService.list(inboundW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getInboundId());
            m.put("type", "入库");
            m.put("title", a.getAssetName());
            m.put("applicant", a.getApplicant());
            m.put("time", a.getApplyTime());
            m.put("status", a.getStatus());
            m.put("statusLabel", "待审核");
            inbounds.add(m);
        }
        result.put("inbound", Map.of("label", "资产入库", "count", inbounds.size(), "items", inbounds));

        // 领用待审批（approve_status=0）
        LambdaQueryWrapper<AssetUseRecord> useW = new LambdaQueryWrapper<>();
        useW.eq(AssetUseRecord::getApproveStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) useW.eq(AssetUseRecord::getSite, currentSite);
        useW.orderByDesc(AssetUseRecord::getCreateTime);
        List<Map<String, Object>> uses = new ArrayList<>();
        for (AssetUseRecord u : assetUseService.list(useW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getRecordId());
            m.put("type", "领用");
            m.put("title", "资产领用 #" + u.getRecordId());
            m.put("applicant", u.getContactPerson());
            m.put("department", u.getDepartment());
            m.put("time", u.getCreateTime());
            m.put("status", u.getApproveStatus());
            m.put("statusLabel", "待审批");
            uses.add(m);
        }
        result.put("use", Map.of("label", "资产领用", "count", uses.size(), "items", uses));

        // 报废待审批（approve_status=0）
        LambdaQueryWrapper<AssetScrapRecord> scrapW = new LambdaQueryWrapper<>();
        scrapW.eq(AssetScrapRecord::getApproveStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) scrapW.eq(AssetScrapRecord::getSite, currentSite);
        scrapW.orderByDesc(AssetScrapRecord::getCreateTime);
        List<Map<String, Object>> scraps = new ArrayList<>();
        for (AssetScrapRecord s : assetScrapService.list(scrapW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getScrapId());
            m.put("type", "报废");
            m.put("title", s.getAssetName() != null ? s.getAssetName() : "报废 #" + s.getScrapId());
            m.put("applicant", s.getApplyUserName());
            m.put("time", s.getCreateTime());
            m.put("status", s.getApproveStatus());
            m.put("statusLabel", "待审批");
            scraps.add(m);
        }
        result.put("scrap", Map.of("label", "资产报废", "count", scraps.size(), "items", scraps));

        // 调拨待审批（transfer_status=0）
        LambdaQueryWrapper<AssetTransferRecord> transferW = new LambdaQueryWrapper<>();
        transferW.eq(AssetTransferRecord::getTransferStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) transferW.eq(AssetTransferRecord::getSite, currentSite);
        transferW.orderByDesc(AssetTransferRecord::getCreateTime);
        List<Map<String, Object>> transfers = new ArrayList<>();
        for (AssetTransferRecord t : assetTransferService.list(transferW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getTransferId());
            m.put("type", "调拨");
            m.put("title", t.getAssetName() != null ? t.getAssetName() : "调拨 #" + t.getTransferId());
            m.put("applicant", t.getApplyUserName());
            m.put("time", t.getCreateTime());
            m.put("status", t.getTransferStatus());
            m.put("statusLabel", "待审批");
            transfers.add(m);
        }
        result.put("transfer", Map.of("label", "资产调拨", "count", transfers.size(), "items", transfers));

        // 资产归还待审批（approve_status=0）
        LambdaQueryWrapper<AssetReturnRecord> retW = new LambdaQueryWrapper<>();
        retW.eq(AssetReturnRecord::getApproveStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) retW.eq(AssetReturnRecord::getSite, currentSite);
        retW.orderByDesc(AssetReturnRecord::getCreateTime);
        List<Map<String, Object>> returns = new ArrayList<>();
        for (AssetReturnRecord r : assetReturnRecordService.list(retW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getReturnId());
            m.put("type", "归还");
            m.put("title", (r.getAssetName() != null ? r.getAssetName() : "资产") + " #" + r.getReturnId());
            m.put("applicant", r.getReturnPerson());
            m.put("department", r.getDepartment());
            m.put("time", r.getCreateTime());
            m.put("status", r.getApproveStatus());
            m.put("statusLabel", "待审批");
            returns.add(m);
        }
        result.put("return", Map.of("label", "资产归还", "count", returns.size(), "items", returns));

        // 耗材领用待审批（approve_status=0）
        LambdaQueryWrapper<ConsumableUseRecord> cuW = new LambdaQueryWrapper<>();
        cuW.eq(ConsumableUseRecord::getApproveStatus, 0);
        if (currentSite != null && !currentSite.isEmpty()) cuW.eq(ConsumableUseRecord::getSite, currentSite);
        cuW.orderByDesc(ConsumableUseRecord::getCreateTime);
        List<Map<String, Object>> cuses = new ArrayList<>();
        for (ConsumableUseRecord c : consumableUseRecordService.list(cuW)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getRecordId());
            m.put("type", "耗材领用");
            m.put("title", (c.getConsumableName() != null ? c.getConsumableName() : "耗材领用") + " x" + c.getQuantity());
            m.put("applicant", c.getApplicant());
            m.put("department", c.getDepartment());
            m.put("time", c.getCreateTime());
            m.put("status", c.getApproveStatus());
            m.put("statusLabel", "待审批");
            cuses.add(m);
        }
        result.put("consumableUse", Map.of("label", "耗材领用", "count", cuses.size(), "items", cuses));

        int totalPending = inbounds.size() + uses.size() + scraps.size() + transfers.size() + returns.size() + cuses.size();
        result.put("totalPending", Map.of("count", totalPending));

        return Result.success(result);
    }

    private String getCurrentSite() {
        Object s = request.getParameter("site");
        return s != null ? s.toString() : null;
    }
}
