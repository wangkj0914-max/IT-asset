package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysDepartment;
import com.asset.itassetsystem.service.SysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.List;

/**
 * 部门控制器
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;
    @Autowired private HttpServletRequest request;

    /**
     * 查询所有部门
     */
    @GetMapping("/list")
    public Result<List<SysDepartment>> list(@RequestParam(required = false) String site, @RequestParam(required = false) Integer status) {
        var w = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDepartment>();
        if (site != null && !site.isEmpty()) w.eq(SysDepartment::getSite, site);
        // 默认只显示启用的，status=0 表示软删除/禁用
        w.eq(SysDepartment::getStatus, status == null ? 1 : status);
        w.orderByAsc(SysDepartment::getSortOrder);
        return Result.success(sysDepartmentService.list(w));
    }

    /**
     * 新增部门
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody SysDepartment department) {
        try {
            if (department.getDeptName() == null || department.getDeptName().trim().isEmpty()) {
                return Result.error("部门名称不能为空");
            }
            // 自动补全站点
            if (department.getSite() == null || department.getSite().isEmpty()) {
                String site = request.getParameter("site");
                if (site == null || site.isEmpty()) {
                    site = request.getHeader("X-Site");
                    if (site != null) site = URLDecoder.decode(site, StandardCharsets.UTF_8);
                }
                if (site != null && !site.isEmpty()) department.setSite(site);
            }
            sysDepartmentService.saveDepartment(department);
            return Result.success("部门创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新部门
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody SysDepartment department) {
        try {
            if (department.getDeptId() == null) {
                return Result.error("部门 ID 不能为空");
            }
            sysDepartmentService.updateDepartment(department);
            return Result.success("部门更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long deptId) {
        try {
            sysDepartmentService.deleteDepartment(deptId);
            return Result.success("部门删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/detail")
    public Result<SysDepartment> detail(@RequestParam Long deptId) {
        SysDepartment department = sysDepartmentService.getDepartmentDetail(deptId);
        if (department == null) {
            return Result.error("部门不存在");
        }
        return Result.success(department);
    }
}
