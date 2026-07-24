package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.ConsumableUseRecord;
import com.asset.itassetsystem.entity.Consumable;
import com.asset.itassetsystem.service.ConsumableUseRecordService;
import com.asset.itassetsystem.service.ConsumableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/consumable-use")
public class ConsumableUseController {

    @Autowired private ConsumableUseRecordService useRecordService;
    @Autowired private ConsumableService consumableService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/page")
    public Result<Page<ConsumableUseRecord>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer approveStatus,
            @RequestParam(required = false) String site) {
        LambdaQueryWrapper<ConsumableUseRecord> w = new LambdaQueryWrapper<>();
        if (approveStatus != null) w.eq(ConsumableUseRecord::getApproveStatus, approveStatus);
        if (site != null && !site.isEmpty()) w.eq(ConsumableUseRecord::getSite, site);
        w.orderByDesc(ConsumableUseRecord::getCreateTime);
        return Result.success(useRecordService.page(new Page<>(current, size), w));
    }

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody ConsumableUseRecord record) {
        if (record.getConsumableId() == null || record.getQuantity() == null || record.getQuantity() <= 0) {
            return Result.fail("参数错误");
        }
        Consumable item = consumableService.getById(record.getConsumableId());
        if (item == null) return Result.fail("耗材不存在");
        if (item.getCurrentStock() < record.getQuantity()) return Result.fail("库存不足");
        record.setApproveStatus(0);
        record.setSite(item.getSite() != null ? item.getSite() : record.getSite());
        record.setConsumableName(item.getConsumableName());
        useRecordService.save(record);
        return Result.success("申请已提交，待审批");
    }

    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long recordId, @RequestParam Boolean approved) {
        ConsumableUseRecord r = useRecordService.getById(recordId);
        if (r == null) return Result.fail("记录不存在");
        if (r.getApproveStatus() != 0) return Result.fail("已处理");
        String user = getCurrentUser();
        r.setApproveStatus(approved ? 1 : 2);
        r.setApproveUser(user);
        r.setApproveTime(LocalDateTime.now());
        useRecordService.updateById(r);

        if (approved) {
            Consumable item = consumableService.getById(r.getConsumableId());
            if (item != null) {
                item.setCurrentStock(item.getCurrentStock() - r.getQuantity());
                consumableService.updateById(item);
            }
        }
        return Result.success(approved ? "已通过，库存已扣减" : "已拒绝");
    }

    private String getCurrentUser() {
        String token = request.getHeader("token");
        if (token != null) {
            try {
                return com.asset.itassetsystem.security.JwtUtil.getUsername(token);
            } catch (Exception ignored) {}
        }
        return request.getParameter("applicant") != null ? request.getParameter("applicant") : "admin";
    }
}
