package com.example.grademanage.Component;

import com.example.grademanage.Factory.FactoryProducer;
import com.example.grademanage.Factory.PermissionFactory;
import javafx.scene.control.MenuItem;
import lombok.Getter;

/**
 * 抽象菜单组件（组合模式）
 * 新增权限过滤逻辑，适配权限工厂
 */
public abstract class MenuComponent {
    // Getter
    @Getter
    protected String name;               // 菜单名称
    protected String permissionCode;     // 菜单绑定的权限编码（如"score:add"）
    protected Integer currentRoleId;     // 当前登录用户的角色ID
    protected PermissionFactory permissionFactory; // 权限工厂实例

    public MenuComponent(String name, String permissionCode, Integer currentRoleId) {
        this.name = name;
        this.permissionCode = permissionCode;
        this.currentRoleId = currentRoleId;
        // 初始化权限工厂（默认使用MySQL）
        this.permissionFactory = (PermissionFactory) FactoryProducer.getFactory(FactoryProducer.FactoryType.PERMISSION);
    }

    /**
     * 核心：权限过滤判断
     * @return true=有权限，false=无权限
     */
    public boolean hasPermission() {
        // 1. 无权限编码的菜单（如菜单组）默认放行
        if (permissionCode == null || permissionCode.isEmpty()) {
            return true;
        }
        // 2. 角色ID为空（未登录），无权限
        if (currentRoleId == null) {
            return false;
        }
        // 3. 调用权限工厂校验权限
        return permissionFactory.checkPermission(currentRoleId, permissionCode);
    }

    /**
     * 渲染菜单（仅有权限时渲染）
     */
    public abstract MenuItem render();

    /**
     * 菜单点击事件
     */
    public abstract void onAction();

    // 组合节点特有方法（叶子节点抛异常）
    public void add(MenuComponent component) {
        throw new UnsupportedOperationException("叶子菜单不支持添加子菜单");
    }

    public void remove(MenuComponent component) {
        throw new UnsupportedOperationException("叶子菜单不支持删除子菜单");
    }

    public MenuComponent getChild(int index) {
        throw new UnsupportedOperationException("叶子菜单无子菜单");
    }
}