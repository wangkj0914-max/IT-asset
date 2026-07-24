package com.asset.itassetsystem;

import java.sql.*;

public class UpdateAdminRole {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/it_asset_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "CHNX#000";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // 更新 admin 角色为管理员
            String updateSql = "UPDATE sys_user SET role = 2 WHERE username = 'admin'";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                int rows = pstmt.executeUpdate();
                System.out.println("更新了 " + rows + " 行");
            }
            
            // 查询验证
            String selectSql = "SELECT user_id, username, real_name, role, status FROM sys_user";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                System.out.println("\n用户列表:");
                System.out.println("ID\t用户名\t姓名\t角色\t状态");
                while (rs.next()) {
                    int roleId = rs.getInt("role");
                    String roleName = roleId == 2 ? "管理员" : "普通用户";
                    System.out.println(rs.getInt("user_id") + "\t" + 
                                     rs.getString("username") + "\t" + 
                                     rs.getString("real_name") + "\t" + 
                                     roleName + "\t" + 
                                     rs.getInt("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
