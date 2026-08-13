package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetModel;
import com.asset.itassetsystem.service.AssetModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产模型控制器
 */
@RestController
@RequestMapping("/assetModel")
public class AssetModelController {

    @Autowired
    private AssetModelService assetModelService;

    @Autowired
    private HttpServletRequest httpRequest;

    private String getSite() {
        String site = httpRequest.getParameter("site");
        return (site != null && !site.isEmpty()) ? site : "苏州";
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<AssetModel>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String site) {
        Page<AssetModel> page = new Page<>(current, size);
        LambdaQueryWrapper<AssetModel> wrapper = new LambdaQueryWrapper<>();
        String effectiveSite = StringUtils.hasText(site) ? site : getSite();
        wrapper.eq(AssetModel::getSite, effectiveSite);
        if (StringUtils.hasText(modelName)) {
            wrapper.like(AssetModel::getModelName, modelName);
        }
        if (categoryId != null) {
            wrapper.eq(AssetModel::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(AssetModel::getCreateTime);
        return Result.success(assetModelService.page(page, wrapper));
    }

    /**
     * 查询全部（按站点）
     */
    @GetMapping("/list")
    public Result<List<AssetModel>> list(@RequestParam(required = false) String site) {
        String effectiveSite = StringUtils.hasText(site) ? site : getSite();
        LambdaQueryWrapper<AssetModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetModel::getSite, effectiveSite);
        wrapper.orderByDesc(AssetModel::getCreateTime);
        return Result.success(assetModelService.list(wrapper));
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody AssetModel model) {
        if (model.getModelName() == null || model.getModelName().trim().isEmpty()) {
            return Result.fail("模型名称不能为空");
        }
        if (model.getSite() == null || model.getSite().isEmpty()) {
            model.setSite(getSite());
        }
        if (model.getEolMonths() == null) model.setEolMonths(36);
        if (model.getDepreciationYears() == null) model.setDepreciationYears(3);
        if (model.getDepreciationMethod() == null) model.setDepreciationMethod("straight_line");
        model.setCreateTime(LocalDateTime.now());
        boolean ok = assetModelService.save(model);
        return ok ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody AssetModel model) {
        if (model.getModelId() == null) return Result.fail("模型ID不能为空");
        model.setUpdateTime(LocalDateTime.now());
        boolean ok = assetModelService.updateById(model);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long modelId) {
        boolean ok = assetModelService.removeById(modelId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 详情
     */
    @GetMapping("/detail")
    public Result<AssetModel> detail(@RequestParam Long modelId) {
        AssetModel model = assetModelService.getById(modelId);
        if (model == null) return Result.fail("模型不存在");
        return Result.success(model);
    }
}
