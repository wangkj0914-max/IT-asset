package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.SysDepartment;
import com.asset.itassetsystem.mapper.SysDepartmentMapper;
import com.asset.itassetsystem.service.SysDepartmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {

    @Override
    public List<SysDepartment> listAll() {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getStatus, 1);
        wrapper.orderByAsc(SysDepartment::getSortOrder);
        return list(wrapper);
    }

    @Override
    public boolean saveDepartment(SysDepartment department) {
        // 检查部门名称在同一站点下是否已存在
        LambdaQueryWrapper<SysDepartment> check = new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getDeptName, department.getDeptName())
                .eq(SysDepartment::getStatus, 1);
        if (department.getSite() != null && !department.getSite().isEmpty()) {
            check.eq(SysDepartment::getSite, department.getSite());
        }
        SysDepartment existing = getOne(check);
        if (existing != null) {
            throw new RuntimeException("部门名称已存在");
        }

        // 设置默认值
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        department.setCreateTime(LocalDateTime.now());

        return save(department);
    }

    @Override
    public boolean updateDepartment(SysDepartment department) {
        if (department.getDeptId() == null) {
            throw new RuntimeException("部门 ID 不能为空");
        }

        SysDepartment existing = getById(department.getDeptId());
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查名称是否被其他部门占用
        if (department.getDeptName() != null && !department.getDeptName().equals(existing.getDeptName())) {
            SysDepartment nameExists = getOne(new LambdaQueryWrapper<SysDepartment>()
                    .eq(SysDepartment::getDeptName, department.getDeptName())
                    .eq(SysDepartment::getStatus, 1));
            if (nameExists != null) {
                throw new RuntimeException("部门名称已存在");
            }
        }

        // 更新字段
        if (department.getDeptName() != null) existing.setDeptName(department.getDeptName());
        if (department.getParentId() != null) existing.setParentId(department.getParentId());
        if (department.getDeptCode() != null) existing.setDeptCode(department.getDeptCode());
        if (department.getManager() != null) existing.setManager(department.getManager());
        if (department.getPhone() != null) existing.setPhone(department.getPhone());
        if (department.getSortOrder() != null) existing.setSortOrder(department.getSortOrder());
        if (department.getStatus() != null) existing.setStatus(department.getStatus());

        return updateById(existing);
    }

    @Override
    public boolean deleteDepartment(Long deptId) {
        SysDepartment existing = getById(deptId);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }

        // 软删除：设置状态为 0
        existing.setStatus(0);
        return updateById(existing);
    }

    @Override
    public SysDepartment getDepartmentDetail(Long deptId) {
        return getById(deptId);
    }
}
