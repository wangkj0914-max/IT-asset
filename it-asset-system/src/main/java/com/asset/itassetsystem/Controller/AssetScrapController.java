package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.ScrapApplyDTO;
import com.asset.itassetsystem.entity.AssetScrapRecord;
import com.asset.itassetsystem.service.AssetScrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资产报废控制器
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/scrap")
public class AssetScrapController {

    @Autowired
    private AssetScrapService assetScrapService;

    @Autowired
    private javax.servlet.http.HttpServletRequest request;

    private String getCurrentUser() {
        Object u = request.getAttribute("username");
        return u != null ? u.toString() : "system";
    }

    /**
     * 分页查询报废记录
     */
    @GetMapping("/page")
    public Result<Object> listPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer scrapType,
                                   @RequestParam(required = false) Integer approveStatus,
                                   @RequestParam(required = false) String site) {
        Object result = assetScrapService.listPage(pageNum, pageSize, scrapType, approveStatus);
        if (site != null && !site.isEmpty() && result != null) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<Object> records = (java.util.List<Object>) result.getClass().getMethod("getRecords").invoke(result);
                if (records != null) {
                    records.removeIf(r -> { try { return !site.equals(r.getClass().getMethod("getSite").invoke(r)); } catch(Exception e) { return false; } });
                    result.getClass().getMethod("setTotal", long.class).invoke(result, (long) records.size());
                }
            } catch(Exception ignored) {}
        }
        return Result.success(result);
    }

    /**
     * 获取报废详情
     */
    @GetMapping("/detail")
    public Result<AssetScrapRecord> detail(@RequestParam Long scrapId) {
        AssetScrapRecord record = assetScrapService.getById(scrapId);
        if (record == null) {
            return Result.fail("报废记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 报废申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@Valid @RequestBody ScrapApplyDTO dto,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String operator = getCurrentUser();
            assetScrapService.apply(dto, operator);
            return Result.success("报废申请提交成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审批报废
     */
    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long scrapId,
                                  @RequestParam Boolean approved,
                                  @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String approver = getCurrentUser();
            assetScrapService.approve(scrapId, approved, approver);
            return Result.success(approved ? "审批通过" : "已拒绝");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询资产的报废记录
     */
    @GetMapping("/list")
    public Result<List<AssetScrapRecord>> listByAssetId(@RequestParam Long assetId) {
        List<AssetScrapRecord> list = assetScrapService.listByAssetId(assetId);
        return Result.success(list);
    }

    /**
     * 查询待审批记录
     */
    @GetMapping("/pending")
    public Result<List<AssetScrapRecord>> listPending() {
        List<AssetScrapRecord> list = assetScrapService.listPending();
        return Result.success(list);
    }
}
