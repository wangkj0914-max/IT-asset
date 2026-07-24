package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.Consumable;
import com.asset.itassetsystem.service.ConsumableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consumable")
public class ConsumableController {

    @Autowired private ConsumableService consumableService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/list")
    public Result<List<Consumable>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String site) {
        var w = new LambdaQueryWrapper<Consumable>();
        if (keyword != null && !keyword.isEmpty()) w.like(Consumable::getConsumableName, keyword);
        if (site != null && !site.isEmpty()) w.eq(Consumable::getSite, site);
        w.orderByDesc(Consumable::getCreateTime);
        return Result.success(consumableService.list(w));
    }

    @GetMapping("/low-stock")
    public Result<List<Consumable>> lowStock() {
        return Result.success(consumableService.list(
            new LambdaQueryWrapper<Consumable>().apply("current_stock <= min_stock")));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody Consumable c) {
        if (c.getConsumableName() == null || c.getConsumableName().isEmpty()) return Result.error("名称不能为空");
        // 自动补全站点（优先 body，其次 param，再 header）
        if (c.getSite() == null || c.getSite().isEmpty()) {
            String site = request.getParameter("site");
            if (site == null || site.isEmpty()) {
                site = request.getHeader("X-Site");
                if (site != null) site = java.net.URLDecoder.decode(site, java.nio.charset.StandardCharsets.UTF_8);
            }
            if (site != null && !site.isEmpty()) c.setSite(site);
        }
        consumableService.save(c);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Consumable c) {
        if (c.getConsumableId() == null) return Result.error("缺少ID");
        consumableService.updateById(c);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long consumableId) {
        consumableService.removeById(consumableId);
        return Result.success("删除成功");
    }

    @PostMapping("/stock-in")
    public Result<String> stockIn(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("consumableId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        String op = (String) body.getOrDefault("operator", "admin");
        consumableService.stockIn(id, qty, op);
        return Result.success("入库成功");
    }

    @PostMapping("/stock-out")
    public Result<String> stockOut(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("consumableId").toString());
        int qty = Integer.parseInt(body.get("quantity").toString());
        String op = (String) body.getOrDefault("operator", "admin");
        try { consumableService.stockOut(id, qty, op); return Result.success("出库成功"); }
        catch (RuntimeException e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/records")
    public Result<?> records(@RequestParam Long consumableId) {
        return Result.success(consumableService.getRecords(consumableId));
    }
}
