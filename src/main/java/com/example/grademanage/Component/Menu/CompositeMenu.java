package com.example.grademanage.Component.Menu;

import com.example.grademanage.Component.MenuComponent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合菜单（菜单组，组合模式）
 * 递归过滤子菜单，子菜单全部无权限则自身也隐藏
 */
public class CompositeMenu extends MenuComponent {
    private List<MenuComponent> children = new ArrayList<>();

    public CompositeMenu(String name, String permissionCode, Integer currentRoleId) {
        super(name, permissionCode, currentRoleId);
    }

    @Override
    public MenuItem render() {
        // 权限过滤：当前菜单组无权限则返回null
        if (!hasPermission()) {
            System.out.println("【权限过滤】角色ID[" + currentRoleId + "]无权限访问菜单组：" + name);
            return null;
        }
        // 创建菜单组
        Menu menu = new Menu(this.name);
        // 递归渲染子菜单，仅添加有权限的子菜单
        for (MenuComponent child : children) {
            MenuItem childItem = child.render();
            if (childItem != null) {
                menu.getItems().add(childItem);
            }
        }
        // 子菜单为空则隐藏当前菜单组
        if (menu.getItems().isEmpty()) {
            System.out.println("【权限过滤】菜单组[" + name + "]无子菜单，隐藏");
            return null;
        }
        return menu;
    }

    @Override
    public void onAction() {
        System.out.println("【菜单操作】展开/折叠菜单组：" + this.name);
    }

    // 重写组合节点方法
    @Override
    public void add(MenuComponent component) {
        children.add(component);
        System.out.println("【菜单管理】菜单组[" + name + "]添加子菜单：" + component.getName());
    }

    @Override
    public void remove(MenuComponent component) {
        children.remove(component);
        System.out.println("【菜单管理】菜单组[" + name + "]删除子菜单：" + component.getName());
    }

    @Override
    public MenuComponent getChild(int index) {
        if (index < 0 || index >= children.size()) {
            throw new IndexOutOfBoundsException("子菜单索引越界：" + index);
        }
        return children.get(index);
    }

    // 获取子菜单数量
    public int getChildCount() {
        return children.size();
    }
}