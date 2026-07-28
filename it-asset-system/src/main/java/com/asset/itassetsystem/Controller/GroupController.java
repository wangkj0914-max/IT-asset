package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysGroup;
import com.asset.itassetsystem.entity.SysGroupPermission;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.mapper.SysGroupPermissionMapper;
import com.asset.itassetsystem.mapper.SysGroupUserMapper;
import com.asset.itassetsystem.security.JwtUtil;
import com.asset.itassetsystem.service.SysGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private SysGroupService sysGroupService;

    @Autowired
    private SysGroupUserMapper sysGroupUserMapper;

    @Autowired
    private SysGroupPermissionMapper sysGroupPermissionMapper;

    @Autowired
    private HttpServletRequest request;

    /**
     * 从请求头获取当前站点
     */
    private String getCurrentSite() {
        String site = request.getParameter("site");
        if (site == null || site.isEmpty()) {
            site = request.getHeader("X-Site");
        }
        if (site != null && !site.isEmpty()) {
            try {
                return java.net.URLDecoder.decode(site, "UTF-8");
            } catch (Exception e) {
                return site;
            }
        }
        return "苏州";
    }

    /**
     * 用户组列表（按 site 隔离）
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        String site = getCurrentSite();
        List<SysGroup> groups = sysGroupService.list(
                new LambdaQueryWrapper<SysGroup>().eq(SysGroup::getSite, site).orderByAsc(SysGroup::getCreateTime));

        List<Map<String, Object>> result = groups.stream().map(g -> {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", g.getGroupId());
            map.put("groupName", g.getGroupName());
            map.put("description", g.getDescription());
            map.put("site", g.getSite());
            map.put("createTime", g.getCreateTime());

            // 成员数
            Long memberCount = sysGroupUserMapper.selectCount(
                    new LambdaQueryWrapper<com.asset.itassetsystem.entity.SysGroupUser>()
                            .eq(com.asset.itassetsystem.entity.SysGroupUser::getGroupId, g.getGroupId()));
            map.put("memberCount", memberCount);

            // 权限数
            Long permCount = sysGroupPermissionMapper.selectCount(
                    new LambdaQueryWrapper<SysGroupPermission>().eq(SysGroupPermission::getGroupId, g.getGroupId()));
            map.put("permCount", permCount);

            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 新建组
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody SysGroup group) {
        try {
            group.setSite(getCurrentSite());
            sysGroupService.saveGroup(group);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新组
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody SysGroup group) {
        try {
            sysGroupService.updateGroup(group);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除组
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params) {
        try {
            Long groupId = params.get("groupId");
            if (groupId == null) {
                return Result.error("组ID不能为空");
            }
            sysGroupService.deleteGroup(groupId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加用户到组
     */
    @PostMapping("/add-user")
    public Result<String> addUser(@RequestBody Map<String, Long> params) {
        try {
            Long groupId = params.get("groupId");
            Long userId = params.get("userId");
            if (groupId == null || userId == null) {
                return Result.error("组ID和用户ID不能为空");
            }
            sysGroupService.addUserToGroup(groupId, userId);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从组移除用户
     */
    @PostMapping("/remove-user")
    public Result<String> removeUser(@RequestBody Map<String, Long> params) {
        try {
            Long groupId = params.get("groupId");
            Long userId = params.get("userId");
            if (groupId == null || userId == null) {
                return Result.error("组ID和用户ID不能为空");
            }
            sysGroupService.removeUserFromGroup(groupId, userId);
            return Result.success("移除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 组成员列表
     */
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users(@RequestParam Long groupId) {
        try {
            List<SysUser> users = sysGroupService.getGroupUsers(groupId);
            List<Map<String, Object>> result = users.stream().map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", u.getUserId());
                map.put("username", u.getUsername());
                map.put("realName", u.getRealName());
                map.put("department", u.getDepartment());
                return map;
            }).collect(Collectors.toList());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取组的权限列表
     */
    @GetMapping("/permissions")
    public Result<Set<String>> permissions(@RequestParam Long groupId) {
        try {
            Set<String> perms = sysGroupService.getGroupPermissions(groupId);
            return Result.success(perms);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置组权限
     */
    @PostMapping("/set-permissions")
    public Result<String> setPermissions(@RequestBody Map<String, Object> params) {
        try {
            Long groupId = Long.valueOf(params.get("groupId").toString());
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) params.get("permissions");
            sysGroupService.setGroupPermissions(groupId, permissions);
            return Result.success("权限设置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
