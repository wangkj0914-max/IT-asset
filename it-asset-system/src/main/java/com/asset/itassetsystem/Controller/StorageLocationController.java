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
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree(@RequestParam(required = false) String site) {
        var w = new LambdaQueryWrapper<StorageLocation>();
        if (site != null && !site.isEmpty()) w.eq(StorageLocation::getSite, site);
        w.orderByAsc(StorageLocation::getSortOrder);
        List<StorageLocation> all = locationService.list(w);

        Map<Long, List<StorageLocation>> parentMap = all.stream()
            .collect(Collectors.groupingBy(l -> l.getParentId() == null ? 0L : l.getParentId()));

        List<Map<String, Object>> result = buildTree(parentMap, 0L);
        return Result.success(result);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<StorageLocation>> parentMap, Long parentId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<StorageLocation> children = parentMap.getOrDefault(parentId, Collections.emptyList());
        for (StorageLocation loc : children) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("locationId", loc.getLocationId());
            node.put("locationName", loc.getLocationName());
            node.put("parentId", loc.getParentId());
            node.put("sortOrder", loc.getSortOrder());
            node.put("remark", loc.getRemark());
            node.put("site", loc.getSite());
            List<Map<String, Object>> sub = buildTree(parentMap, loc.getLocationId());
            if (!sub.isEmpty()) node.put("children", sub);
            list.add(node);
        }
        return list;
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody StorageLocation location) {
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty())
            return Result.fail("名称不能为空");
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
        if (location.getLocationId() == null) return Result.fail("缺少ID");
        locationService.updateById(location);
        return Result.success("更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long locationId) {
        // 检查是否有子地点
        var childW = new LambdaQueryWrapper<StorageLocation>();
        childW.eq(StorageLocation::getParentId, locationId);
        if (locationService.count(childW) > 0) {
            return Result.fail("该地点下存在子地点，请先删除子地点");
        }
        locationService.removeById(locationId);
        return Result.success("删除成功");
    }
}
