package com.example.grademanage.Component;

import lombok.Getter;

/**
 * 组合模式 权限抽象组件（简洁版）
 */
@Getter
public abstract class PermissionComponent {
    protected String permissionCode;

    public PermissionComponent(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    // 校验是否包含目标权限
    public abstract boolean containsPermission(String targetCode);

    // 添加子权限（仅组合节点实现）
    public void add(PermissionComponent component) {
        throw new UnsupportedOperationException();
    }

    // 获取子权限（仅组合节点实现）
    public PermissionComponent getChild(int index) {
        throw new UnsupportedOperationException();
    }

}