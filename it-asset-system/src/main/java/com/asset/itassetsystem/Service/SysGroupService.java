package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.SysGroup;
import com.asset.itassetsystem.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Set;

public interface SysGroupService extends IService<SysGroup> {

    /**
     * 查询用户所属的用户组ID列表
     */
    List<Long> getUserGroupIds(Long userId);

    /**
     * 查询组的权限标识列表
     */
    Set<String> getGroupPermissions(Long groupId);

    /**
     * 检查用户是否拥有指定权限
     * 管理员(role=2)自动拥有所有权限
     */
    boolean hasPermission(Long userId, Integer role, String permission);

    /**
     * 添加用户到组
     */
    boolean addUserToGroup(Long groupId, Long userId);

    /**
     * 从组移除用户
     */
    boolean removeUserFromGroup(Long groupId, Long userId);

    /**
     * 获取组成员列表
     */
    List<SysUser> getGroupUsers(Long groupId);

    /**
     * 设置组的权限
     */
    boolean setGroupPermissions(Long groupId, List<String> permissions);

    /**
     * 保存组
     */
    boolean saveGroup(SysGroup group);

    /**
     * 更新组
     */
    boolean updateGroup(SysGroup group);

    /**
     * 删除组
     */
    boolean deleteGroup(Long groupId);
}
