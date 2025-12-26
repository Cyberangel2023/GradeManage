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
     * 校验角色是否拥有指定权限编码
     * @param roleId 角色ID
     * @param permissionCode 权限编码
     * @return true=有权限，false=无权限
     */
    boolean checkPermission(Integer roleId, String permissionCode);

    /**
     * 查询角色的所有权限关联关系
     * @param roleId 角色ID
     * @return 角色-权限关联列表
     */
    List<RolePermission> getRolePermissions(Integer roleId);

    /**
     * 查询所有权限信息
     * @return 权限列表
     */
    List<Permission> getAllPermissions();
}