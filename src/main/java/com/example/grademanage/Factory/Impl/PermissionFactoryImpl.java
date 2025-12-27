package com.example.grademanage.Factory.Impl;

import com.example.grademanage.Component.PermissionCombination.CompositePermission;
import com.example.grademanage.Component.PermissionCombination.LeafPermission;
import com.example.grademanage.Entity.Permission;
import com.example.grademanage.Factory.PermissionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限工厂实现类（保留原数据，适配组合模式）
 */
public class PermissionFactoryImpl implements PermissionFactory {
    private static PermissionFactoryImpl instance;

    public static PermissionFactoryImpl getInstance() {
        if (instance == null) {
            instance = new PermissionFactoryImpl();
        }
        return instance;
    }

    // 保留原数据不变
    @Override
    public List<String> getPermissionCodesByRoleId(Integer roleId) {
        List<String> permissionCodes = new ArrayList<>();
        if (roleId == 1) {
            permissionCodes.add("view");
        } else if (roleId == 2) {
            permissionCodes.add("save");
            permissionCodes.add("delete");
            permissionCodes.add("query");
            permissionCodes.add("update");
            permissionCodes.add("view");
        } else if (roleId == 3) {
            permissionCodes.add("query");
            permissionCodes.add("view");
        }
        return permissionCodes;
    }

    // 保留原数据不变
    @Override
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(1, "增", "save", "增加"));
        permissions.add(new Permission(2, "删", "delete", "删除"));
        permissions.add(new Permission(3, "查", "query", "查询"));
        permissions.add(new Permission(4, "改", "update", "修改"));
        permissions.add(new Permission(5, "看", "view", "查看"));
        return permissions;
    }

    // 组合模式实现权限校验
    @Override
    public boolean checkPermission(Integer roleId, String targetPermission) {
        if (roleId == null || targetPermission == null) {
            return false;
        }
        // 获取角色权限编码
        List<String> rolePerms = getPermissionCodesByRoleId(roleId);
        if (rolePerms.isEmpty()) {
            return false;
        }
        // 构建组合权限树
        CompositePermission root = new CompositePermission("role_perm:" + roleId);
        for (String permCode : rolePerms) {
            root.add(new LeafPermission(permCode));
        }
        // 校验权限
        return root.containsPermission(targetPermission);
    }
}