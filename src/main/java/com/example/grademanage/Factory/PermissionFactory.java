package com.example.grademanage.Factory;

import com.example.grademanage.Entity.Permission;
import com.example.grademanage.Entity.RolePermission;

import java.util.List;

/**
 * 权限工厂抽象接口（抽象工厂模式）
 * 统一提供角色权限查询、权限校验能力
 */
public interface PermissionFactory {
    /**
     * 根据角色ID查询该角色拥有的所有权限编码
     * @param roleId 角色ID
     * @return 权限编码列表（如 ["score:add", "score:query"]）
     */
    List<String> getPermissionCodesByRoleId(Integer roleId);

    /**
     * 查询所有权限信息
     * @return 权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 权限校验
     * @return 权限是否拥有
     */
    // 新增权限校验方法
    boolean checkPermission(Integer roleId, String targetPermission);
}