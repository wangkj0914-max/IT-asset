package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.StorageLocation;
import com.asset.itassetsystem.service.StorageLocationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/storage-location")
public class StorageLocationController {

    @Autowired private StorageLocationService locationService;
    @Autowired private HttpServletRequest request;

    @GetMapping("/list")
    public Result<List<StorageLocation>> list(@RequestParam(required = false) String site) {
        var w = new LambdaQueryWrapper<StorageLocation>();
        if (site != null && !site.isEmpty()) w.eq(StorageLocation::getSite, site);
        w.orderByAsc(StorageLocation::getSortOrder);
        return Result.success(locationService.list(w));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody StorageLocation location) {
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty())
            return Result.error("名称不能为空");
        // 自动补全站点
        if (location.getSite() == null || location.getSite().isEmpty()) {
            String site = request.getParameter("site");
            if (site == null || site.isEmpty()) {
                site = request.getHeader("X-Site");
                if (site != null) site = URLDecoder.decode(site, StandardCharsets.UTF_8);
            }
            if (site != null && !site.isEmpty()) location.setSite(site);
        }
        locationService.save(location);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody StorageLocation location) {
        if (location.getLocationId() == null) return Result.error("缺少ID");
        locationService.updateById(location);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long locationId) {
        locationService.removeById(locationId);
        return Result.success("删除成功");
    }
}
