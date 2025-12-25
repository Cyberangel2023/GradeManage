package com.example.grademanage.DAO;

import com.example.grademanage.Entity.User;
import com.example.grademanage.Util.ORMUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户DAO层（修正命名规范 + 完善增删改查 + 优化资源管理）
 */
public class UserDAO {
    /**
     * 保存用户信息（新增）
     */
    public void save(User user) {
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createSaveSQL(user))) {
            pstmt.executeUpdate();
            System.out.println("用户数据保存成功，执行SQL：" + ORMUtil.createSaveSQL(user));
        } catch (Exception e) {
            throw new RuntimeException("保存用户数据失败，用户ID：" + (user.getUserId() == null ? "未设置" : user.getUserId()), e);
        }
    }

    /**
     * 根据ID删除用户
     */
    public void delete(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("删除用户失败：用户ID不能为null");
        }
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createDeleteSQL(User.class, userId))) {

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("删除用户失败：未找到ID为" + userId + "的用户");
            } else {
                System.out.println("用户数据删除成功，执行SQL：" + ORMUtil.createDeleteSQL(User.class, userId));
            }
        } catch (Exception e) {
            throw new RuntimeException("删除用户数据失败，用户ID：" + userId, e);
        }
    }

    /**
     * 更新用户信息
     */
    public void update(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("更新用户失败：用户ID不能为null");
        }
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createUpdateSQL(user))) {

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("更新用户失败：未找到ID为" + user.getUserId() + "的用户");
            } else {
                System.out.println("用户数据修改成功，执行SQL：" + ORMUtil.createUpdateSQL(user));
            }
        } catch (Exception e) {
            throw new RuntimeException("修改用户数据失败，用户ID：" + user.getUserId(), e);
        }
    }

    /**
     * 根据ID查询单个用户
     */
    public User findById(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("查询用户失败：用户ID不能为null");
        }
        String sql = ORMUtil.createSelectSQL(User.class, userId);
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // 解析结果集为User对象
            if (rs.next()) {
                return resultSetToUser(rs);
            } else {
                System.out.println("未找到ID为" + userId + "的用户");
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("根据ID查询用户失败，用户ID：" + userId, e);
        }
    }

    /**
     * 根据真实姓名模糊查询用户（支持多结果）
     */
    public List<User> findByRealName(String realName) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new IllegalArgumentException("查询用户失败：姓名不能为null/空");
        }
        // 构建模糊查询SQL
        String tableName = (String) ORMUtil.tableMetaCache.get("User").get("tableName");
        String sql = String.format("SELECT * FROM %s WHERE real_name LIKE ?", tableName);
        List<User> userList = new ArrayList<>();

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置模糊查询参数（防止SQL注入）
            pstmt.setString(1, "%" + realName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                // 解析结果集
                while (rs.next()) {
                    userList.add(resultSetToUser(rs));
                }
            }
            System.out.println("根据姓名[" + realName + "]查询到" + userList.size() + "个用户");
            return userList;
        } catch (Exception e) {
            throw new RuntimeException("根据姓名查询用户失败，姓名：" + realName, e);
        }
    }

    /**
     * 查询全部用户
     */
    public List<User> findAll() {
        String sql = ORMUtil.createSelectAllSQL(User.class);
        List<User> userList = new ArrayList<>();

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // 解析所有结果
            while (rs.next()) {
                userList.add(resultSetToUser(rs));
            }
            System.out.println("查询到全部用户共" + userList.size() + "个");
            return userList;
        } catch (Exception e) {
            throw new RuntimeException("查询全部用户失败", e);
        }
    }

    /**
     * 工具方法：将ResultSet解析为User对象（复用逻辑，避免重复代码）
     */
    private User resultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setRealName(rs.getString("real_name"));
        user.setPassword(rs.getString("password"));
        user.setGender(rs.getString("gender"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setRoleId(rs.getInt("role_id"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        user.setUpdateTime(rs.getTimestamp("update_time"));
        return user;
    }
}