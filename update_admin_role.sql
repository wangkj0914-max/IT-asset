-- 检查并更新 admin 用户的角色为管理员（role=2）
UPDATE sys_user SET role = 2 WHERE username = 'admin';

-- 验证更新结果
SELECT user_id, username, real_name, role, status FROM sys_user;
