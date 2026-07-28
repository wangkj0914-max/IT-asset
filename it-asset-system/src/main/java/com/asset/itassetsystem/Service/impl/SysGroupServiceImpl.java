package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.SysGroup;
import com.asset.itassetsystem.entity.SysGroupPermission;
import com.asset.itassetsystem.entity.SysGroupUser;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.mapper.SysGroupMapper;
import com.asset.itassetsystem.mapper.SysGroupPermissionMapper;
import com.asset.itassetsystem.mapper.SysGroupUserMapper;
import com.asset.itassetsystem.mapper.SysUserMapper;
import com.asset.itassetsystem.service.SysGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysGroupServiceImpl extends ServiceImpl<SysGroupMapper, SysGroup> implements SysGroupService {

    @Autowired
    private SysGroupUserMapper sysGroupUserMapper;

    @Autowired
    private SysGroupPermissionMapper sysGroupPermissionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<Long> getUserGroupIds(Long userId) {
        List<SysGroupUser> mappings = sysGroupUserMapper.selectList(
                new LambdaQueryWrapper<SysGroupUser>().eq(SysGroupUser::getUserId, userId));
        return mappings.stream().map(SysGroupUser::getGroupId).collect(Collectors.toList());
    }

    @Override
    public Set<String> getGroupPermissions(Long groupId) {
        List<SysGroupPermission> perms = sysGroupPermissionMapper.selectList(
                new LambdaQueryWrapper<SysGroupPermission>().eq(SysGroupPermission::getGroupId, groupId));
        return perms.stream().map(SysGroupPermission::getPermission).collect(Collectors.toSet());
    }

    @Override
    public boolean hasPermission(Long userId, Integer role, String permission) {
        // 管理员(role=2)自动拥有所有权限（向后兼容）
        if (role != null && role == 2) {
            return true;
        }

        // 获取用户所属组
        List<Long> groupIds = getUserGroupIds(userId);
        if (groupIds.isEmpty()) {
            return false;
        }

        // 检查任一组的权限
        for (Long groupId : groupIds) {
            Set<String> perms = getGroupPermissions(groupId);
            if (perms.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addUserToGroup(Long groupId, Long userId) {
        // 检查是否已存在
        SysGroupUser existing = sysGroupUserMapper.selectOne(new LambdaQueryWrapper<SysGroupUser>()
                .eq(SysGroupUser::getGroupId, groupId)
                .eq(SysGroupUser::getUserId, userId));
        if (existing != null) {
            return true; // 已存在，视为成功
        }
        SysGroupUser mapping = new SysGroupUser();
        mapping.setGroupId(groupId);
        mapping.setUserId(userId);
        return sysGroupUserMapper.insert(mapping) > 0;
    }

    @Override
    public boolean removeUserFromGroup(Long groupId, Long userId) {
        return sysGroupUserMapper.delete(new LambdaQueryWrapper<SysGroupUser>()
                .eq(SysGroupUser::getGroupId, groupId)
                .eq(SysGroupUser::getUserId, userId)) > 0;
    }

    @Override
    public List<SysUser> getGroupUsers(Long groupId) {
        List<SysGroupUser> mappings = sysGroupUserMapper.selectList(
                new LambdaQueryWrapper<SysGroupUser>().eq(SysGroupUser::getGroupId, groupId));
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = mappings.stream().map(SysGroupUser::getUserId).collect(Collectors.toList());
        return sysUserMapper.selectBatchIds(userIds);
    }

    @Override
    public boolean setGroupPermissions(Long groupId, List<String> permissions) {
        // 先删除该组的所有现有权限
        sysGroupPermissionMapper.delete(new LambdaQueryWrapper<SysGroupPermission>()
                .eq(SysGroupPermission::getGroupId, groupId));

        // 插入新权限
        if (permissions != null && !permissions.isEmpty()) {
            for (String perm : permissions) {
                SysGroupPermission p = new SysGroupPermission();
                p.setGroupId(groupId);
                p.setPermission(perm);
                sysGroupPermissionMapper.insert(p);
            }
        }
        return true;
    }

    @Override
    public boolean saveGroup(SysGroup group) {
        group.setCreateTime(LocalDateTime.now());
        return save(group);
    }

    @Override
    public boolean updateGroup(SysGroup group) {
        if (group.getGroupId() == null) {
            throw new RuntimeException("组ID不能为空");
        }
        return updateById(group);
    }

    @Override
    public boolean deleteGroup(Long groupId) {
        // 删除组的用户映射和权限
        sysGroupUserMapper.delete(new LambdaQueryWrapper<SysGroupUser>().eq(SysGroupUser::getGroupId, groupId));
        sysGroupPermissionMapper.delete(new LambdaQueryWrapper<SysGroupPermission>().eq(SysGroupPermission::getGroupId, groupId));
        return removeById(groupId);
    }
}
