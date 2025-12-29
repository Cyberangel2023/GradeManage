package com.example.grademanage.Controller;

import com.example.grademanage.Entity.User;
import com.example.grademanage.Factory.BeanFactory;
import com.example.grademanage.Factory.Impl.BeanFactoryImpl;
import com.example.grademanage.Service.UserService;
import com.example.grademanage.Util.Util;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 登录界面控制器
 * 负责处理登录界面的用户交互逻辑
 */
public class LoginController implements Initializable {
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

    private static UserService userService;

    private String role;

    /**
     * 初始化方法
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image backgroundImage = null;
        // 优先从 classpath 读取（兼容打包）
        URL imageUrl = getClass().getResource("/img/background.jpg");
        if (imageUrl != null) {
            backgroundImage = new Image(imageUrl.toExternalForm());
        } else {
            // 降级读取（避免NPE）
            backgroundImage = new Image("img/background.jpg", true);
        }

        // 空值兜底：如果图片仍加载失败，不抛异常，仅提示
        if (backgroundImage.isError()) {
            System.err.println("警告：背景图片加载失败（路径：img/background.jpg），请检查资源文件是否存在！");
        } else {
            // 假设你有一个背景ImageView控件（如名为backgroundImg）
            background.setImage(backgroundImage);
            background.setOpacity(0.5);
        }

        usernameInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
        passwordInput.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

        loginBtn.setOnAction(e -> handleLogin());
        registerBtn.setOnAction(e -> handleRegister());
        helpBtn.setOnAction(e -> handleHelp());

        BeanFactory beanFactory = BeanFactoryImpl.getInstance();
        userService = beanFactory.getBean("userService");
    }

    /**
     * 处理登录按钮点击事件
     */
    private void handleLogin() {
        // 获取输入的用户名和密码
        String userId = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();

        // 输入验证
        if (userId.isEmpty() || password.isEmpty()) {
            Util.showAlert(AlertType.ERROR, "登录失败", "用户名和密码不能为空！");
            return;
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            Util.showAlert(AlertType.ERROR, "登录失败", "用户名不存在！");
            clearInputFields(); // 清空输入框
            return;
        }
        if (user.getPassword().equals(password)) {
            clearInputFields(); // 清空输入框
            Util.setUser(user);
            switchToView(); // 登录成功后跳转到主界面
        } else {
            Util.showAlert(AlertType.ERROR, "登录失败", "密码错误！");
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
                Util.showAlert(AlertType.ERROR, "路径错误", "打包后未找到/fxml/register.fxml文件，请检查resources目录结构！");
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
            Util.showAlert(AlertType.WARNING, "跳转失败", "注册页面加载出错，请重试！");
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
                Util.showAlert(AlertType.ERROR, "路径错误", "打包后未找到/fxml/view.fxml文件，请检查resources目录结构！");
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
            Util.showAlert(AlertType.WARNING, "跳转失败", "成绩管理系统页面加载出错，请重试！");
        }
    }

    /**
     * 处理帮助按钮点击事件
     */
    private void handleHelp() {
        Util.showAlert(AlertType.INFORMATION, "帮助信息",
                """
                        1. 用户名：输入您的学号或工号
                        2. 密码：输入您的登录密码
                        3. 如忘记密码，请联系管理员
                        """);
    }

    /**
     * 清空输入框内容
     */
    private void clearInputFields() {
        usernameInput.clear();
        passwordInput.clear();
    }
}
