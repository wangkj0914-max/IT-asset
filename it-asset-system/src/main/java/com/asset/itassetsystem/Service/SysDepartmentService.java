package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.SysDepartment;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 部门服务接口
 */
public interface SysDepartmentService extends IService<SysDepartment> {

    /**
     * 查询所有部门
     */
    List<SysDepartment> listAll();

    /**
     * 保存部门（新增）
     * @param department 部门实体
     * @return true=成功
     */
    boolean saveDepartment(SysDepartment department);

    /**
     * 更新部门
     * @param department 部门实体
     * @return true=成功
     */
    boolean updateDepartment(SysDepartment department);

    /**
     * 删除部门
     * @param deptId 部门 ID
     * @return true=成功
     */
    boolean deleteDepartment(Long deptId);

    /**
     * 获取部门详情
     * @param deptId 部门 ID
     * @return 部门实体，不存在返回 null
     */
    SysDepartment getDepartmentDetail(Long deptId);
}
