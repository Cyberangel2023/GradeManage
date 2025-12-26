package com.example.grademanage.Controller;
import com.example.grademanage.Entity.User;
import com.example.grademanage.Factory.BeanFactory;
import com.example.grademanage.Factory.Impl.BeanFactoryImpl;
import com.example.grademanage.Service.UserService;
import com.example.grademanage.Util.Util;
import com.example.grademanage.Util.ValiDateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
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
    private TextField nameInput; // 姓名
    @FXML
    private TextField phoneInput; // 手机号
    @FXML
    private TextField emailInput; // 邮箱
    @FXML
    private TextField usernameInput; // 用户名输入框（学号/工号）
    @FXML
    private PasswordField passwordInput; // 密码输入框
    @FXML
    private PasswordField confirmPasswordInput; // 确认密码输入框
    @FXML
    private ChoiceBox<String> genderChoose; // 性别选择下拉框
    @FXML
    private ChoiceBox<String> roleChoose; // 角色选择下拉框

    @FXML
    private Button registerBtn; // 注册按钮
    @FXML
    private Button helpBtn; // 帮助按钮
    @FXML
    private Button backBtn; // 返回按钮

    private static UserService userService;

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
        // 填充性别选择下拉框选项
        initGenderChoiceBox();

        // 绑定按钮点击事件
        registerBtn.setOnAction(e -> handleRegister());
        helpBtn.setOnAction(e -> handleHelp());
        backBtn.setOnAction(e -> handleBack());

        BeanFactory beanFactory = BeanFactoryImpl.getInstance();
        userService = beanFactory.getBean("userService");
    }

    /**
     * 初始化性别选择下拉框
     */
    private void initGenderChoiceBox() {
        // 添加性别选项
        genderChoose.getItems().addAll("男", "女");
        // 设置默认选中项
        if (!genderChoose.getItems().isEmpty()) {
            genderChoose.setValue(genderChoose.getItems().get(0));
        }
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
        // 获取输入内容并去除首尾空格
        String name = nameInput.getText();
        String phone = phoneInput.getText();
        String email = emailInput.getText();
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText().trim();
        String confirmPassword = confirmPasswordInput.getText().trim();
        Integer roleId = switch (roleChoose.getValue()) {
            case "学生" -> 1;
            case "教师" -> 2;
            case "辅导员" -> 3;
            default -> throw new IllegalStateException("Unexpected value: " + roleChoose.getValue());
        };
        String gender = genderChoose.getValue();

        // 输入合法性验证
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty()) {
            Util.showAlert(Alert.AlertType.ERROR, "注册失败", "请输入完整信信息！");
        }

        // 用户名合法性校验
        if (!ValiDateUtil.validateUserId(username)) {
            Util.showAlert(Alert.AlertType.ERROR, "注册失败", "用户名必须为数字！");
        }

        // 用户名存在性校验
        if (userService.getUserById(username) != null) {
            Util.showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在！");
        }

        // 密码一致性校验
        if (!password.equals(confirmPassword)) {
            Util.showAlert(Alert.AlertType.ERROR, "注册失败", "两次输入的密码不一致，请重新输入！");
            confirmPasswordInput.clear(); // 清空确认密码框
            return;
        }

        // 密码强度校验
        String comment = ValiDateUtil.validatePassword(password);
        if (!comment.isEmpty()) {
            Util.showAlert(Alert.AlertType.ERROR, "注册失败", comment);
        }

        User user = new User();
        user.setUserId(username);
        user.setRealName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(password);
        user.setGender(gender);
        user.setRoleId(roleId);

        userService.saveUser(user);
        handleBack();
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
        Util.showAlert(Alert.AlertType.INFORMATION, "注册帮助", helpContent);
    }

    /**
     * 处理返回按钮点击事件
     * 关闭当前注册窗口，返回登录界面
     */
    private void handleBack() {
        try {
            // 获取当前Stage
            Stage currentStage = (Stage) registerBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/fxml/login.fxml")
            );

            // 打包后路径校验：如果找不到FXML文件，主动提示
            if (loader.getLocation() == null) {
                Util.showAlert(Alert.AlertType.ERROR, "路径错误", "打包后未找到/fxml/login.fxml文件，请检查resources目录结构！");
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
            currentStage.setTitle("登录");
        } catch (IOException e) {
            e.printStackTrace();
            Util.showAlert(Alert.AlertType.WARNING, "跳转失败", "登录页面加载出错，请重试！");
        }
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