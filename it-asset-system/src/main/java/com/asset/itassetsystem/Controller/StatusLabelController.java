package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.StatusLabel;
import com.asset.itassetsystem.service.StatusLabelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 状态标签控制器
 */
@RestController
@RequestMapping("/statusLabel")
public class StatusLabelController {

    @Autowired
    private StatusLabelService statusLabelService;

    @Autowired
    private HttpServletRequest httpRequest;

    private String getSite() {
        String site = httpRequest.getParameter("site");
        return (site != null && !site.isEmpty()) ? site : "苏州";
    }

    /**
     * 查询全部（按站点）
     */
    @GetMapping("/list")
    public Result<List<StatusLabel>> list(@RequestParam(required = false) String site) {
        String effectiveSite = StringUtils.hasText(site) ? site : getSite();
        LambdaQueryWrapper<StatusLabel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatusLabel::getSite, effectiveSite);
        wrapper.orderByAsc(StatusLabel::getStatusType);
        return Result.success(statusLabelService.list(wrapper));
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody StatusLabel label) {
        if (label.getStatusName() == null || label.getStatusName().trim().isEmpty()) {
            return Result.fail("状态名称不能为空");
        }
        if (label.getSite() == null || label.getSite().isEmpty()) {
            label.setSite(getSite());
        }
        if (label.getStatusType() == null) label.setStatusType(0);
        if (label.getColor() == null) label.setColor("info");
        if (label.getIsDefault() == null) label.setIsDefault(0);
        label.setCreateTime(LocalDateTime.now());
        boolean ok = statusLabelService.save(label);
        return ok ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody StatusLabel label) {
        if (label.getStatusLabelId() == null) return Result.fail("状态标签ID不能为空");
        label.setUpdateTime(LocalDateTime.now());
        boolean ok = statusLabelService.updateById(label);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long statusLabelId) {
        boolean ok = statusLabelService.removeById(statusLabelId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}
