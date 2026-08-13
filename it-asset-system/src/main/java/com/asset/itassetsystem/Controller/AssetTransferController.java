package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.TransferApplyDTO;
import com.asset.itassetsystem.entity.AssetTransferRecord;
import com.asset.itassetsystem.service.AssetTransferService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 资产调拨控制器
 */
@RestController
@RequestMapping("/transfer")
public class AssetTransferController {

    @Autowired
    private AssetTransferService assetTransferService;

    /**
     * 提交调拨申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody TransferApplyDTO dto) {
        try {
            if (dto.getAssetId() == null) {
                return Result.fail("请选择要调拨的资产");
            }
            if (dto.getToDepartment() == null || dto.getToDepartment().isEmpty()) {
                return Result.fail("请填写调入部门");
            }
            assetTransferService.apply(dto);
            return Result.success("调拨申请已提交");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审批调拨
     */
    @PostMapping("/approve")
    public Result<String> approve(
            @RequestParam Long transferId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        try {
            if (status != 1 && status != 2) {
                return Result.fail("审批状态错误");
            }
            assetTransferService.approve(transferId, status, remark);
            return Result.success(status == 1 ? "已通过" : "已拒绝");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除调拨记录
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long transferId) {
        try {
            assetTransferService.delete(transferId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询调拨记录
     */
    @GetMapping("/page")
    public Result<IPage<AssetTransferRecord>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String site) {

        Page<AssetTransferRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetTransferRecord> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(AssetTransferRecord::getTransferStatus, status);
        }
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetTransferRecord::getSite, site);
        }
        wrapper.orderByDesc(AssetTransferRecord::getCreateTime);

        IPage<AssetTransferRecord> result = assetTransferService.page(page, wrapper);
        return Result.success(result);
    }
}
