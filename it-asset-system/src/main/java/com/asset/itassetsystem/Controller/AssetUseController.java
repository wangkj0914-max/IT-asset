package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.UseApplyDTO;
import com.asset.itassetsystem.entity.AssetUseRecord;
import com.asset.itassetsystem.service.AssetUseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 资产领用控制器
 */
@RestController
@RequestMapping("/use")
public class AssetUseController {

    @Autowired
    private AssetUseService assetUseService;

    @Autowired
    private javax.servlet.http.HttpServletRequest request;

    private String getCurrentUser() {
        Object u = request.getAttribute("username");
        return u != null ? u.toString() : "system";
    }

    /**
     * 领用申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@Valid @RequestBody UseApplyDTO dto,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String operator = getCurrentUser();
            assetUseService.apply(dto, operator);
            return Result.success("领用申请提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批领用
     */
    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long recordId,
                                  @RequestParam Boolean approved,
                                  @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String approver = getCurrentUser();
            assetUseService.approve(recordId, approved, approver);
            return Result.success(approved ? "审批通过" : "已拒绝");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除领用记录
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long recordId) {
        AssetUseRecord r = assetUseService.getById(recordId);
        if (r == null) return Result.fail("记录不存在");
        if (r.getApproveStatus() != null && r.getApproveStatus() == 1) return Result.fail("已审批通过的记录不能删除");
        assetUseService.removeById(recordId);
        return Result.success("删除成功");
    }

    /**
     * 资产归还
     */
    @PostMapping("/return")
    public Result<String> returnAsset(@RequestParam Long assetId,
                                      @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String operator = getCurrentUser();
            assetUseService.returnAsset(assetId, operator);
            return Result.success("归还成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询资产的领用记录
     */
    @GetMapping("/list")
    public Result<List<AssetUseRecord>> listByAssetId(@RequestParam Long assetId) {
        List<AssetUseRecord> list = assetUseService.listByAssetId(assetId);
        return Result.success(list);
    }

    /**
     * 查询所有领用记录（分页，包含资产名称）
     */
    @GetMapping({"/list-all", "/page"})
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.asset.itassetsystem.vo.UseRecordVO>> listAll(
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) Integer overdue,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        com.baomidou.mybatisplus.core.metadata.IPage<com.asset.itassetsystem.vo.UseRecordVO> result = 
            assetUseService.listAllWithAssetInfo(current, size, assetName, status, overdue);
        if (site != null && !site.isEmpty() && result.getRecords() != null) {
            var filtered = result.getRecords().stream()
                .filter(r -> site.equals(r.getSite()))
                .collect(java.util.stream.Collectors.toList());
            result.setRecords(filtered);
            result.setTotal((long) filtered.size());
        }
        return Result.success(result);
    }

    /**
     * 查询待审批记录
     */
    @GetMapping("/pending")
    public Result<List<AssetUseRecord>> listPending() {
        List<AssetUseRecord> list = assetUseService.listPending();
        return Result.success(list);
    }

}
