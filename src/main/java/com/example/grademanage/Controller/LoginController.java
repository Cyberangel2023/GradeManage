package com.example.grademanage.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 登录界面控制器
 * 负责处理登录界面的用户交互逻辑
 */
public class LoginController implements Initializable {
    // 绑定FXML文件中对应的控件
    @FXML
    private ImageView background; // 背景图片控件
    @FXML
    private TextField usernameInput; // 用户名输入框
    @FXML
    private PasswordField passwordInput; // 密码输入框
    @FXML
    private Button loginBtn; // 登录按钮
    @FXML
    private Button registerBtn; // 注册按钮
    @FXML
    private Button helpBtn; // 帮助按钮

    /**
     * 初始化方法
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
        passwordInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

        loginBtn.setOnAction(e -> handleLogin());
        registerBtn.setOnAction(e -> handleRegister());
        helpBtn.setOnAction(e -> handleHelp());
    }

    /**
     * 处理登录按钮点击事件
     */
    private void handleLogin() {
        // 获取输入的用户名和密码
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();

        // 简单的输入验证
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "登录失败", "用户名和密码不能为空，请填写完整！");
            return;
        }

        // 这里是登录逻辑的占位符
        // 实际项目中需要替换为真实的验证逻辑（比如连接数据库、调用接口等）
        if (username.equals("user") && password.equals("123456")) {
            // 登录成功后跳转到主界面
            switchToView();
            clearInputFields(); // 清空输入框
        } else {
            showAlert(AlertType.ERROR, "登录失败", "用户名或密码错误，请重试！");
            passwordInput.clear(); // 只清空密码框
        }
    }

    /**
     * 处理注册按钮点击事件
     */
    private void handleRegister() {
        try {
            // 获取当前Stage
            Stage currentStage = (Stage) registerBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/fxml/register.fxml") // 替换成你要跳转的register.fxml
            );

            // 打包后路径校验：如果找不到FXML文件，主动提示
            if (loader.getLocation() == null) {
                showAlert(AlertType.ERROR, "路径错误", "打包后未找到/fxml/register.fxml文件，请检查resources目录结构！");
                return;
            }
            Parent newRoot = loader.load();

            // 替换Scene根节点
            if (currentStage.getScene() == null) {
                // 打包后若Scene为空，新建Scene
                currentStage.setScene(new Scene(newRoot));
            } else {
                currentStage.getScene().setRoot(newRoot);
            }
            // 设置窗口标题
            currentStage.setTitle("用户注册");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.WARNING, "跳转失败", "注册页面加载出错，请重试！");
        }
    }

    private void switchToView() {
        try {
            // 获取当前Stage
            Stage currentStage = (Stage) registerBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/fxml/view.fxml") // 替换成你要跳转的register.fxml
            );

            // 打包后路径校验：如果找不到FXML文件，主动提示
            if (loader.getLocation() == null) {
                showAlert(AlertType.ERROR, "路径错误", "打包后未找到/fxml/view.fxml文件，请检查resources目录结构！");
                return;
            }
            Parent newRoot = loader.load();

            // 替换Scene根节点
            if (currentStage.getScene() == null) {
                // 打包后若Scene为空，新建Scene
                currentStage.setScene(new Scene(newRoot, 1080, 720));
            } else {
                currentStage.getScene().setRoot(newRoot);
                currentStage.setWidth(1080);
                currentStage.setHeight(720);
                currentStage.centerOnScreen();
            }
            // 设置窗口标题
            currentStage.setTitle("成绩管理系统");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.WARNING, "跳转失败", "成绩管理系统页面加载出错，请重试！");
        }
    }

    /**
     * 处理帮助按钮点击事件
     */
    private void handleHelp() {
        showAlert(AlertType.INFORMATION, "帮助信息",
                """
                        1. 用户名：输入您的学号或工号
                        2. 密码：输入您的登录密码
                        3. 如忘记密码，请联系管理员
                        """);
    }

    /**
     * 通用的弹窗提示方法
     * @param alertType 弹窗类型（信息、错误、警告等）
     * @param title 弹窗标题
     * @param content 弹窗内容
     */
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // 隐藏头部文本
        alert.setContentText(content);
        alert.showAndWait(); // 等待用户关闭弹窗
    }

    /**
     * 清空输入框内容
     */
    private void clearInputFields() {
        usernameInput.clear();
        passwordInput.clear();
    }
}
