package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetCategory;
import com.asset.itassetsystem.service.AssetCategoryService;
import com.asset.itassetsystem.service.AssetInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 资产分类控制器
 */
@RestController
@RequestMapping("/category")
public class AssetCategoryController {

    @Autowired
    private AssetCategoryService assetCategoryService;

    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired private HttpServletRequest request;

    /**
     * 查询所有分类
     */
    @GetMapping("/list")
    public Result<List<AssetCategory>> list(@RequestParam(required = false) String site) {
        List<AssetCategory> list = assetCategoryService.listAll(site);
        return Result.success(list);
    }
    
    /**
     * 查询一级分类
     */
    @GetMapping("/parent")
    public Result<List<AssetCategory>> listParent() {
        List<AssetCategory> list = assetCategoryService.listParentCategories();
        return Result.success(list);
    }
    
    /**
     * 新增分类
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody AssetCategory category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return Result.fail("分类名称不能为空");
        }
        // 自动补全站点
        if (category.getSite() == null || category.getSite().isEmpty()) {
            String site = request.getParameter("site");
            if (site == null || site.isEmpty()) {
                site = request.getHeader("X-Site");
                if (site != null) site = URLDecoder.decode(site, StandardCharsets.UTF_8);
            }
            if (site != null && !site.isEmpty()) category.setSite(site);
        }
        assetCategoryService.save(category);
        return Result.success("添加成功");
    }
    
    /**
     * 更新分类
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody AssetCategory category) {
        if (category.getCategoryId() == null) {
            return Result.fail("分类 ID 不能为空");
        }
        assetCategoryService.updateById(category);
        return Result.success("更新成功");
    }
    
    /**
     * 删除分类
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long categoryId) {
        // 检查该分类下是否有资产
        long assetCount = assetInfoService.count(new LambdaQueryWrapper<com.asset.itassetsystem.entity.AssetInfo>()
            .eq(com.asset.itassetsystem.entity.AssetInfo::getCategoryId, categoryId));
        if (assetCount > 0) {
            return Result.fail("该分类下有 " + assetCount + " 条资产，无法删除");
        }

        // 子分类自动提升为顶级
        List<AssetCategory> children = assetCategoryService.list().stream()
            .filter(c -> categoryId.equals(c.getParentId()))
            .toList();
        for (AssetCategory child : children) {
            child.setParentId(0L);
            assetCategoryService.updateById(child);
        }
        assetCategoryService.removeById(categoryId);
        return Result.success("删除成功");
    }
}
