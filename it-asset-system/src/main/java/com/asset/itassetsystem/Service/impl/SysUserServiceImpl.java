package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.SysUser;
import com.asset.itassetsystem.mapper.SysUserMapper;
import com.asset.itassetsystem.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Override
    public SysUser login(String username, String password) {
        SysUser user = baseMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            return null;
        }

        // 检查是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return null; // 账号已被锁定
        }

        boolean success = false;

        // 根据密码哈希类型选择验证方式
        String hashType = user.getPasswordHashType();

        if ("BCRYPT".equals(hashType)) {
            success = passwordEncoder.matches(password, user.getPassword());
        } else {
            success = md5(password).equals(user.getPassword());
            if (success) {
                user.setPassword(passwordEncoder.encode(password));
                user.setPasswordHashType("BCRYPT");
                user.setUpdateTime(LocalDateTime.now());
            }
        }

        // 登录失败锁定：5次失败锁定15分钟
        if (success) {
            user.setLoginFailCount(0);
            user.setLockedUntil(null);
        } else {
            int fails = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            user.setLoginFailCount(fails);
            if (fails >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
        }
        updateById(user);

        return success ? user : null;
    }

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public IPage<SysUser> pageUsers(Long current, Long size, String keyword, String site) {
        IPage<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getRealName, keyword)
            );
        }

        if (site != null && !site.isEmpty()) {
            wrapper.eq(SysUser::getSite, site);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean updateRole(Long userId, Integer role) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不能修改自己的角色为普通用户（防止最后一个管理员失去权限）
        if (role == 1) {
            long adminCount = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 2));
            if (adminCount <= 1 && user.getRole() == 2) {
                throw new RuntimeException("至少需要保留一个管理员");
            }
        }

        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不能删除管理员
        if (user.getRole() == 2) {
            throw new RuntimeException("不能删除管理员账户");
        }

        return removeById(userId);
    }

    @Override
    public boolean createUser(SysUser user) {
        // 检查用户名是否存在
        SysUser existing = getByUsername(user.getUsername());
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值，使用 BCrypt 加密密码
        String rawPassword = user.getPassword() != null ? user.getPassword() : "123456";
        user.setPassword(encodePassword(rawPassword));
        user.setPasswordHashType("BCRYPT");
        user.setStatus(1);
        user.setRole(user.getRole() != null ? user.getRole() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return save(user);
    }

    /**
     * BCrypt 密码加密
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * MD5 加密（仅用于旧密码兼容验证）
     */
    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }
}
