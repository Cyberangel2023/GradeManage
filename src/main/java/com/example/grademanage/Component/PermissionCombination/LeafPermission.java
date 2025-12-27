package com.example.grademanage.Component.PermissionCombination;

import com.example.grademanage.Component.PermissionComponent;

/**
 * 组合模式 叶子节点 - 具体权限
 */
public class LeafPermission extends PermissionComponent {

    public LeafPermission(String permissionCode) {
        super(permissionCode);
    }

    @Override
    public boolean containsPermission(String targetCode) {
        if (targetCode == null || this.permissionCode == null) {
            return false;
        }
        return this.permissionCode.equals(targetCode);
    }
}