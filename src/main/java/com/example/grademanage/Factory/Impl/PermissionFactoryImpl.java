package com.example.grademanage.Factory.Impl;

import com.example.grademanage.Entity.Permission;
import com.example.grademanage.Entity.RolePermission;
import com.example.grademanage.Factory.PermissionFactory;
import com.example.grademanage.Util.ORMUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL实现的权限工厂（具体工厂）
 * 适配你的Role、Permission、RolePermission实体
 */
public class PermissionFactoryImpl implements PermissionFactory {
    private static PermissionFactoryImpl instance;

    public static PermissionFactoryImpl getInstance() {
        if (instance == null) {
            instance = new PermissionFactoryImpl();
        }
        return instance;
    }

    @Override
    public List<String> getPermissionCodesByRoleId(Integer roleId) {
        List<String> permissionCodes = new ArrayList<>();
        // 关联查询：角色权限 -> 权限表，获取权限编码
        String sql = "SELECT p.permission_code " +
                "FROM role_permission rp " +
                "JOIN permission p ON rp.permission_id = p.permission_id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                permissionCodes.add(rs.getString("permission_code"));
            }
        } catch (Exception e) {
            throw new RuntimeException("查询角色[" + roleId + "]权限编码失败", e);
        }
        return permissionCodes;
    }

    @Override
    public boolean checkPermission(Integer roleId, String permissionCode) {
        // 先查询角色所有权限编码，再判断是否包含目标编码
        List<String> ownCodes = getPermissionCodesByRoleId(roleId);
        return ownCodes.contains(permissionCode);
    }

    @Override
    public List<RolePermission> getRolePermissions(Integer roleId) {
        List<RolePermission> rolePermissions = new ArrayList<>();
        String sql = "SELECT rp_id, role_id, permission_id FROM role_permission WHERE role_id = ?";

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // 适配你的RolePermission构造方法
                RolePermission rp = new RolePermission(
                        rs.getInt("rp_id"),
                        rs.getInt("role_id"),
                        rs.getInt("permission_id")
                );
                rolePermissions.add(rp);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询角色[" + roleId + "]权限关联失败", e);
        }
        return rolePermissions;
    }

    @Override
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT permission_id, permission_name, permission_code, permission_desc FROM permission";

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // 适配你的Permission构造方法
                Permission permission = new Permission(
                        rs.getInt("permission_id"),
                        rs.getString("permission_name"),
                        rs.getString("permission_code"),
                        rs.getString("permission_desc")
                );
                permissions.add(permission);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询所有权限失败", e);
        }
        return permissions;
    }
}