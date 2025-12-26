package com.example.grademanage.Util;

import javafx.scene.control.Alert;

public class Util {
    /**
     * 通用的弹窗提示方法
     * @param alertType 弹窗类型（信息、错误、警告等）
     * @param title 弹窗标题
     * @param content 弹窗内容
     */
    public static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // 隐藏头部文本
        alert.setContentText(content);
        alert.showAndWait(); // 等待用户关闭弹窗
    }
}
