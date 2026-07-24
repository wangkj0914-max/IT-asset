package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.dto.LoginDTO;
import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.security.JwtUtil;
import com.asset.itassetsystem.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户登录
     */
    @PostMapping
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 验证输入是否为空
        if (loginDTO.getUsername() == null || loginDTO.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 检查是否因暴力破解被锁定
        SysUser preCheck = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername().trim()));
        if (preCheck != null && preCheck.getLockedUntil() != null && preCheck.getLockedUntil().isAfter(LocalDateTime.now())) {
            long min = java.time.Duration.between(LocalDateTime.now(), preCheck.getLockedUntil()).toMinutes();
            return Result.error("账号已锁定，请" + min + "分钟后重试");
        }

        // 用户登录
        SysUser user = sysUserService.login(loginDTO.getUsername().trim(), loginDTO.getPassword());
        if (user == null) {
            return Result.error("账号或密码不正确");
        }

        // 使用 JWT 生成 token（静态方法）
        String token = JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("userId", user.getUserId());
        // 判断本次登录用的密码是否为系统默认值
        boolean isDefault = "123456".equals(loginDTO.getPassword().trim()) || "admin".equals(loginDTO.getPassword().trim());
        data.put("isDefaultPassword", isDefault);

        return Result.success(data);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }

        // 使用 JwtUtil 解析 token（静态方法）
        Long userId = JwtUtil.getUserId(token);
        if (userId == null) {
            return Result.error("Token 无效");
        }

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("userRole", user.getRole()); // 前端使用 userRole 字段
        data.put("department", user.getDepartment());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());

        return Result.success(data);
    }
}
