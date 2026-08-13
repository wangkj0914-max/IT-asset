package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.Consumable;
import com.asset.itassetsystem.entity.ConsumableRecord;
import com.asset.itassetsystem.mapper.ConsumableRecordMapper;
import com.asset.itassetsystem.service.ConsumableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/consumable")
public class ConsumableController {

    @Autowired private ConsumableService consumableService;
    @Autowired private ConsumableRecordMapper consumableRecordMapper;
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
        if (c.getConsumableName() == null || c.getConsumableName().isEmpty()) return Result.fail("名称不能为空");
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
        if (c.getConsumableId() == null) return Result.fail("缺少ID");
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
        catch (RuntimeException e) { return Result.fail(e.getMessage()); }
    }

    @GetMapping("/records")
    public Result<?> records(@RequestParam Long consumableId) {
        return Result.success(consumableService.getRecords(consumableId));
    }

    /**
     * GET /consumable/trend - 耗材消耗趋势
     * 基于出库记录(type=2)，按月分组统计出库数量和金额
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "12") int months) {
        if (months <= 0) months = 12;
        LocalDateTime since = LocalDateTime.now().minusMonths(months).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<ConsumableRecord> records = consumableRecordMapper.selectList(
            new LambdaQueryWrapper<ConsumableRecord>()
                .eq(ConsumableRecord::getType, 2)
                .ge(ConsumableRecord::getCreateTime, since));

        // 获取所有耗材ID对应的价格
        Set<Long> consumableIds = new HashSet<>();
        for (ConsumableRecord r : records) {
            if (r.getConsumableId() != null) consumableIds.add(r.getConsumableId());
        }
        Map<Long, BigDecimal> priceMap = new HashMap<>();
        if (!consumableIds.isEmpty()) {
            for (Consumable c : consumableService.listByIds(consumableIds)) {
                priceMap.put(c.getConsumableId(), c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO);
            }
        }

        // 按月汇总
        Map<String, Map<String, Object>> monthlyMap = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            String month = LocalDate.now().minusMonths(i).format(monthFmt);
            Map<String, Object> m = new HashMap<>();
            m.put("month", month);
            m.put("totalQuantity", 0);
            m.put("totalAmount", BigDecimal.ZERO);
            monthlyMap.put(month, m);
        }

        for (ConsumableRecord r : records) {
            if (r.getCreateTime() != null) {
                String key = r.getCreateTime().format(monthFmt);
                Map<String, Object> m = monthlyMap.get(key);
                if (m != null) {
                    int qty = r.getQuantity() != null ? r.getQuantity() : 0;
                    m.put("totalQuantity", (int) m.get("totalQuantity") + qty);
                    BigDecimal price = priceMap.getOrDefault(r.getConsumableId(), BigDecimal.ZERO);
                    BigDecimal amount = price.multiply(BigDecimal.valueOf(qty));
                    m.put("totalAmount", ((BigDecimal) m.get("totalAmount")).add(amount));
                }
            }
        }

        return Result.success(new ArrayList<>(monthlyMap.values()));
    }

    /**
     * GET /consumable/alerts - 低库存告警（含建议补充量）
     */
    @GetMapping("/alerts")
    public Result<List<Map<String, Object>>> alerts(@RequestParam(required = false) String site) {
        var w = new LambdaQueryWrapper<Consumable>().apply("current_stock <= min_stock");
        if (site != null && !site.isEmpty()) w.eq(Consumable::getSite, site);

        List<Consumable> lowStock = consumableService.list(w);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Consumable c : lowStock) {
            Map<String, Object> m = new HashMap<>();
            m.put("consumableId", c.getConsumableId());
            m.put("consumableName", c.getConsumableName());
            m.put("currentStock", c.getCurrentStock());
            m.put("minStock", c.getMinStock());
            m.put("suggestedReplenishment", (c.getMinStock() != null && c.getCurrentStock() != null) 
                ? c.getMinStock() - c.getCurrentStock() : 0);
            m.put("unit", c.getUnit());
            m.put("category", c.getCategory());
            m.put("price", c.getPrice());
            result.add(m);
        }
        return Result.success(result);
    }
}
