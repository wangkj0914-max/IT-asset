package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.security.JwtUtil;
import com.asset.itassetsystem.service.SysUserService;
import com.asset.itassetsystem.service.impl.SysUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserServiceImpl sysUserServiceImpl;

    @Autowired private HttpServletRequest request;

    /**
     * 获取当前操作用户角色（2=管理员，1=普通用户）
     */
    private Integer getCurrentRole() {
        Object role = request.getAttribute("role");
        return role != null ? Integer.valueOf(role.toString()) : 1;
    }

    /**
     * 获取当前操作用户的归属站点
     */
    private String getCurrentUserSite() {
        Object username = request.getAttribute("username");
        if (username == null) return null;
        SysUser u = sysUserService.getOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.toString())
        );
        return u != null ? u.getSite() : null;
    }

    /**
     * 获取用户列表（分页） — 普通用户只能看到同站点用户
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String site) {

        // 普通用户强制限定为本人归属站点
        if (getCurrentRole() != 2) {
            site = getCurrentUserSite();
        }

        var page = sysUserService.pageUsers(current, size, keyword, site);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());

        return Result.success(data);
    }

    /**
     * 获取所有用户列表（不分页） — 普通用户只能看到同站点用户
     */
    @GetMapping("/all")
    public Result<List<SysUser>> listAll() {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
            .orderByDesc(SysUser::getCreateTime);
        if (getCurrentRole() != 2) {
            String site = getCurrentUserSite();
            if (site != null && !site.isEmpty()) {
                wrapper.eq(SysUser::getSite, site);
            }
        }
        return Result.success(sysUserService.list(wrapper));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<SysUser> getById(@PathVariable Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Result<String> create(@RequestBody SysUser user) {
        try {
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            // 自动补全站点
            if (user.getSite() == null || user.getSite().isEmpty()) {
                String site = request.getParameter("site");
                if (site == null || site.isEmpty()) {
                    site = request.getHeader("X-Site");
                    if (site != null) site = URLDecoder.decode(site, StandardCharsets.UTF_8);
                }
                if (site != null && !site.isEmpty()) user.setSite(site);
            }
            // 普通用户只能创建同站点用户，且不能创建管理员
            if (getCurrentRole() != 2) {
                String mySite = getCurrentUserSite();
                if (mySite == null || !mySite.equals(user.getSite())) {
                    return Result.fail("普通用户只能创建本站点用户");
                }
                user.setRole(1);
            }
            sysUserService.createUser(user);
            return Result.success("用户创建成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 修改用户角色（仅管理员可操作）
     */
    @PostMapping("/update-role")
    public Result<String> updateRole(
            @RequestHeader(value = "token", required = false) String token,
            @RequestParam Long userId,
            @RequestParam Integer role) {
        try {
            // 检查操作者是否为管理员
            if (token != null && !token.isEmpty()) {
                Integer operatorRole = JwtUtil.getRole(token);
                if (operatorRole == null || operatorRole != 2) {
                    return Result.fail("无权限：仅管理员可修改用户角色");
                }
            }

            if (role == null || (role != 1 && role != 2)) {
                return Result.fail("角色参数错误");
            }
            sysUserService.updateRole(userId, role);
            return Result.success(role == 2 ? "已设置为管理员" : "已设置为普通用户");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody SysUser user) {
        try {
            if (user.getUserId() == null) {
                return Result.fail("用户 ID 不能为空");
            }
            SysUser existing = sysUserService.getById(user.getUserId());
            if (existing == null) {
                return Result.fail("用户不存在");
            }
            // 更新允许修改的字段
            existing.setRealName(user.getRealName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            existing.setDepartment(user.getDepartment());
            sysUserService.updateById(existing);
            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long userId) {
        try {
            sysUserService.deleteUser(userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 重置用户密码（使用 BCrypt）
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam Long userId) {
        try {
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 自动生成 8 位随机密码
            String newPwd = generateRandomPwd(8);
            user.setPassword(sysUserServiceImpl.encodePassword(newPwd));
            user.setPasswordHashType("BCRYPT");
            sysUserService.updateById(user);
            return Result.success("密码已重置为: " + newPwd);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 自己修改密码（需验证旧密码）
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> body) {
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (oldPwd == null || newPwd == null || newPwd.length() < 6) {
            return Result.fail("密码至少6位");
        }
        Object uid = request.getAttribute("userId");
        if (uid == null) return Result.fail("未登录");
        Long userId = Long.valueOf(uid.toString());
        SysUser user = sysUserService.getById(userId);
        if (user == null) return Result.fail("用户不存在");
        // BCrypt 验证旧密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPwd, user.getPassword())) {
            return Result.fail("原密码不正确");
        }
        user.setPassword(encoder.encode(newPwd));
        user.setPasswordHashType("BCRYPT");
        sysUserService.updateById(user);
        return Result.success("密码修改成功");
    }

    private String generateRandomPwd(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return sb.toString();
    }

    /**
     * 更新用户姓名
     */
    @PostMapping("/update-name")
    public Result<String> updateName(
            @RequestParam Long userId,
            @RequestParam String realName) {
        try {
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            user.setRealName(realName);
            sysUserService.updateById(user);
            return Result.success("姓名更新成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
