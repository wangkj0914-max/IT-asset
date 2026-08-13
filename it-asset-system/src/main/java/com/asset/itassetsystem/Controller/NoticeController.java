package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysNotice;
import com.asset.itassetsystem.service.SysNoticeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private SysNoticeService noticeService;

    @GetMapping("/page")
    public Result<IPage<SysNotice>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String site) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysNotice::getTitle, keyword);
        }
        if (site != null && !site.isEmpty()) {
            wrapper.eq(SysNotice::getSite, site);
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);
        return Result.success(noticeService.page(page, wrapper));
    }

    @GetMapping("/published")
    public Result<IPage<SysNotice>> published(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "5") Long pageSize) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1).orderByDesc(SysNotice::getCreateTime);
        return Result.success(noticeService.page(page, wrapper));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody SysNotice notice) {
        if (notice.getTitle() == null || notice.getTitle().isEmpty()) return Result.fail("标题不能为空");
        notice.setCreateTime(LocalDateTime.now());
        noticeService.save(notice);
        return Result.success("公告创建成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody SysNotice notice) {
        if (notice.getNoticeId() == null) return Result.fail("缺少ID");
        notice.setUpdateTime(LocalDateTime.now());
        noticeService.updateById(notice);
        return Result.success("公告更新成功");
    }

    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long noticeId) {
        noticeService.removeById(noticeId);
        return Result.success("删除成功");
    }

    @GetMapping("/detail")
    public Result<SysNotice> detail(@RequestParam Long noticeId) {
        return Result.success(noticeService.getById(noticeId));
    }
}
