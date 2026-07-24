package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户信息（不含密码）
     */
    SysUser login(String username, String password);
    
    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);
    
    /**
     * 分页查询用户列表
     */
    IPage<SysUser> pageUsers(Long current, Long size, String keyword, String site);
    
    /**
     * 修改用户角色
     */
    boolean updateRole(Long userId, Integer role);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
    
    /**
     * 创建用户
     */
    boolean createUser(SysUser user);
}
