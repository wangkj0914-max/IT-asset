package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.License;
import com.asset.itassetsystem.service.LicenseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/license")
public class LicenseController {

    @Autowired private LicenseService licenseService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/list")
    public Result<List<License>> list(@RequestParam(required = false) String site) {
        var w = new LambdaQueryWrapper<License>();
        if (site != null && !site.isEmpty()) w.eq(License::getSite, site);
        w.orderByAsc(License::getExpireDate);
        return Result.success(licenseService.list(w));
    }

    @GetMapping("/expiring")
    public Result<List<License>> expiring(@RequestParam(defaultValue = "30") int days,
                                          @RequestParam(required = false) String site) {
        return Result.success(licenseService.getExpiring(days, site));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody License l) {
        if (l.getSoftwareName() == null || l.getSoftwareName().isEmpty()) return Result.fail("名称不能为空");
        // 自动补全站点
        if (l.getSite() == null || l.getSite().isEmpty()) {
            String site = request.getParameter("site");
            if (site == null || site.isEmpty()) {
                site = request.getHeader("X-Site");
                if (site != null) site = URLDecoder.decode(site, StandardCharsets.UTF_8);
            }
            if (site != null && !site.isEmpty()) l.setSite(site);
        }
        licenseService.save(l);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody License l) {
        if (l.getLicenseId() == null) return Result.fail("缺少ID");
        licenseService.updateById(l);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long licenseId) {
        licenseService.removeById(licenseId);
        return Result.success("删除成功");
    }
}
