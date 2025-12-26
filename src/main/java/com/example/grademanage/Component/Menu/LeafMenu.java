package com.example.grademanage.Component.Menu;

import com.example.grademanage.Component.MenuComponent;
import javafx.scene.control.MenuItem;

/**
 * 叶子菜单（组合模式）
 * 仅当角色有权限时才渲染
 */
public class LeafMenu extends MenuComponent {

    public LeafMenu(String name, String permissionCode, Integer currentRoleId) {
        super(name, permissionCode, currentRoleId);
    }

    @Override
    public MenuItem render() {
        // 权限过滤：无权限则返回null（不渲染）
        if (!hasPermission()) {
            System.out.println("【权限过滤】角色ID[" + currentRoleId + "]无权限访问菜单：" + name + "（权限编码：" + permissionCode + "）");
            return null;
        }
        // 有权限则创建并返回MenuItem
        MenuItem menuItem = new MenuItem(this.name);
        menuItem.setOnAction(e -> this.onAction());
        return menuItem;
    }

    @Override
    public void onAction() {
        System.out.println("【菜单操作】执行：" + this.name + "（权限编码：" + this.permissionCode + "）");
        // 可扩展：调用对应业务逻辑（如ScoreService.addScore()）
        switch (permissionCode) {
            case "score:add" -> System.out.println("→ 执行新增成绩逻辑");
            case "score:query" -> System.out.println("→ 执行查询成绩逻辑");
            case "user:add" -> System.out.println("→ 执行新增用户逻辑");
            default -> System.out.println("→ 未知权限编码，无对应逻辑");
        }
    }
}