package com.example.grademanage.Component.PermissionCombination;

import com.example.grademanage.Component.PermissionComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合模式 组合节点 - 权限组
 */
public class CompositePermission extends PermissionComponent {
    private List<PermissionComponent> children = new ArrayList<>();

    public CompositePermission(String permissionCode) {
        super(permissionCode);
    }

    @Override
    public boolean containsPermission(String targetCode) {
        // 自身匹配
        if (this.permissionCode.equals(targetCode)) {
            return true;
        }
        // 递归匹配子节点
        for (PermissionComponent child : children) {
            if (child.containsPermission(targetCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void add(PermissionComponent component) {
        children.add(component);
    }
}