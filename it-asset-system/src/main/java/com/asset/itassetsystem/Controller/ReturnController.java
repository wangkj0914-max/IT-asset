package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetReturnRecord;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetReturnRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 资产归还 — 提交 + 审批
 */
@RestController
@RequestMapping("/return")
public class ReturnController {

    @Autowired private AssetReturnRecordService returnRecordService;
    @Autowired private AssetInfoService assetInfoService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/page")
    public Result<Page<AssetReturnRecord>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer approveStatus,
            @RequestParam(required = false) String site) {
        LambdaQueryWrapper<AssetReturnRecord> w = new LambdaQueryWrapper<>();
        if (approveStatus != null) w.eq(AssetReturnRecord::getApproveStatus, approveStatus);
        if (site != null && !site.isEmpty()) w.eq(AssetReturnRecord::getSite, site);
        w.orderByDesc(AssetReturnRecord::getCreateTime);
        return Result.success(returnRecordService.page(new Page<>(current, size), w));
    }

    @GetMapping("/detail")
    public Result<AssetReturnRecord> detail(@RequestParam Long returnId) {
        AssetReturnRecord r = returnRecordService.getById(returnId);
        if (r == null) return Result.fail("记录不存在");
        return Result.success(r);
    }

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody AssetReturnRecord record) {
        if (record.getAssetId() == null) return Result.fail("缺少资产ID");
        AssetInfo asset = assetInfoService.getById(record.getAssetId());
        if (asset == null) return Result.fail("资产不存在");
        record.setAssetCode(asset.getAssetCode());
        record.setAssetName(asset.getAssetName());
        record.setReturnDate(LocalDateTime.now());
        record.setApproveStatus(0);
        returnRecordService.save(record);
        return Result.success("归还申请已提交，待审批");
    }

    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long returnId, @RequestParam Boolean approved) {
        AssetReturnRecord r = returnRecordService.getById(returnId);
        if (r == null) return Result.fail("记录不存在");
        if (r.getApproveStatus() != 0) return Result.fail("已处理");
        String user = getCurrentUser();
        r.setApproveStatus(approved ? 1 : 2);
        r.setApproveUser(user);
        r.setApproveTime(LocalDateTime.now());
        returnRecordService.updateById(r);

        if (approved && r.getAssetId() != null) {
            AssetInfo asset = assetInfoService.getById(r.getAssetId());
            if (asset != null) {
                asset.setStatus(0); // 0=在库
                assetInfoService.updateById(asset);
            }
        }
        return Result.success(approved ? "已通过，资产已回库" : "已拒绝");
    }

    private String getCurrentUser() {
        String token = request.getHeader("token");
        if (token != null) {
            try {
                return com.asset.itassetsystem.security.JwtUtil.getUsername(token);
            } catch (Exception ignored) {}
        }
        return "admin";
    }
}
