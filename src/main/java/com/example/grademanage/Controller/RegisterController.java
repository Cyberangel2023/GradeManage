package com.example.grademanage.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 注册界面控制器
 * 负责处理注册界面的用户交互逻辑，包括输入验证、注册提交、返回登录页等功能
 */
public class RegisterController implements Initializable {

    // 绑定FXML文件中的控件
    @FXML
    private ImageView background; // 背景图片控件
    @FXML
    private TextField usernameInput; // 用户名输入框（学号/工号）
    @FXML
    private PasswordField passwordInput; // 密码输入框
    @FXML
    private PasswordField confirmPasswordInput; // 确认密码输入框
    @FXML
    private ChoiceBox<String> roleChoose; // 角色选择下拉框

    @FXML
    private Button registerBtn; // 注册按钮
    @FXML
    private Button helpBtn; // 帮助按钮
    @FXML
    private Button backBtn; // 返回按钮

    /**
     * 初始化方法
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化控件样式
        usernameInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
        passwordInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
        confirmPasswordInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
        roleChoose.setStyle("-fx-font-size: 12px; -fx-padding: 3px;");

        // 填充角色选择下拉框选项
        initRoleChoiceBox();

        // 绑定按钮点击事件
        registerBtn.setOnAction(e -> handleRegister());
        helpBtn.setOnAction(e -> handleHelp());
        backBtn.setOnAction(e -> handleBack());
    }

    /**
     * 初始化角色选择下拉框
     */
    private void initRoleChoiceBox() {
        // 添加角色选项
        roleChoose.getItems().addAll("学生", "教师", "辅导员");
        // 设置默认选中项
        if (!roleChoose.getItems().isEmpty()) {
            roleChoose.setValue(roleChoose.getItems().get(0));
        }
    }

    /**
     * 处理注册按钮点击事件
     * 包含输入验证、密码一致性校验、注册逻辑处理
     */
    private void handleRegister() {
        // 1. 获取输入内容并去除首尾空格
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();
        String confirmPassword = confirmPasswordInput.getText().trim();
        String role = roleChoose.getValue();

        // 2. 输入合法性验证
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "注册失败", "用户名不能为空！请输入学号/工号。");
            return;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "注册失败", "密码不能为空！");
            return;
        }
        if (confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "注册失败", "确认密码不能为空！");
            return;
        }
        if (role == null || role.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "注册失败", "请选择用户角色！");
            return;
        }

        // 3. 密码一致性校验
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "注册失败", "两次输入的密码不一致，请重新输入！");
            confirmPasswordInput.clear(); // 清空确认密码框
            return;
        }

        // 4. 密码强度简单校验（可选，可根据需求调整）
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "提示", "密码长度建议不少于6位，以提高安全性！");
            // 仅提示，不阻断注册（也可改为阻断）
        }

        // 5. 执行注册逻辑（此处为模拟，实际项目需替换为数据库插入操作）
        boolean registerSuccess = simulateRegister(username, password, role);

        // 6. 注册结果反馈
        if (registerSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "注册成功", "恭喜您，" + username + "（" + role + "）注册成功！\n请返回登录界面登录。");
            clearInputFields(); // 清空输入框
        } else {
            showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在，请更换用户名重试！");
            usernameInput.clear(); // 清空用户名框
        }
    }

    /**
     * 处理帮助按钮点击事件
     */
    private void handleHelp() {
        String helpContent = """
                注册帮助信息：
                1. 用户名：请输入您的学号（学生）或工号（教师/管理员）
                2. 密码：建议长度不少于6位，包含数字/字母
                3. 确认密码：需与密码输入框内容完全一致
                4. 角色选择：根据实际身份选择对应角色
                """;
        showAlert(Alert.AlertType.INFORMATION, "注册帮助", helpContent);
    }

    /**
     * 处理返回按钮点击事件
     * 关闭当前注册窗口，返回登录界面
     */
    private void handleBack() {
        // 获取当前窗口并关闭
        Stage currentStage = (Stage) backBtn.getScene().getWindow();
        currentStage.close();

        // 实际项目中可在此处打开登录界面（需结合主程序逻辑）
        // 示例：LoginApplication.showLoginView();
    }

    /**
     * 模拟注册逻辑（实际项目需替换为数据库操作）
     * @param username 用户名
     * @param password 密码
     * @param role 角色
     * @return 注册是否成功
     */
    private boolean simulateRegister(String username, String password, String role) {
        // 模拟：仅当用户名不是"test"时注册成功（演示用）
        return !username.equals("test");
    }

    /**
     * 通用弹窗提示方法
     * @param alertType 弹窗类型（信息、错误、警告等）
     * @param title 弹窗标题
     * @param content 弹窗内容
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // 隐藏头部文本，简化弹窗样式
        alert.setContentText(content);
        alert.showAndWait(); // 等待用户关闭弹窗
    }

    /**
     * 清空所有输入框内容
     */
    private void clearInputFields() {
        usernameInput.clear();
        passwordInput.clear();
        confirmPasswordInput.clear();
        // 重置角色选择为默认项
        if (!roleChoose.getItems().isEmpty()) {
            roleChoose.setValue(roleChoose.getItems().get(0));
        }
    }
}