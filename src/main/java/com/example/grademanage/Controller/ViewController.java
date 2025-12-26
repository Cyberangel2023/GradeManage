package com.example.grademanage.Controller;

import com.example.grademanage.Entity.Score;
import com.example.grademanage.Util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * 成绩管理主界面控制器（Java 23适配版）
 * 实现成绩表格展示、增删改查、搜索、排序、退出登录等功能（无测试数据）
 */
public class ViewController implements Initializable {
    @FXML
    private Label realname; // 显示当前登录用户姓名
    @FXML
    private TableView<Score> scoreTable; // 成绩表格

    @FXML
    private TableColumn<Score, String> id; // 学号列
    @FXML
    private TableColumn<Score, String> name; // 姓名列
    @FXML
    private TableColumn<Score, String> course; // 课程列
    @FXML
    private TableColumn<Score, Double> score; // 成绩列
    @FXML
    private TableColumn<Score, String> type; // 类型列
    @FXML
    private TableColumn<Score, String> teacher; // 老师列
    @FXML
    private TableColumn<Score, String> time; // 时间列

    @FXML
    private TextField idSearchInput; // 按学号搜索输入框
    @FXML
    private TextField nameSearchInput; // 按姓名搜索输入框
    @FXML
    private TextField courseSearchInput; // 按课程搜索输入框

    @FXML
    private Label sortType; // 当前排序方式标签

    @FXML
    private Button addBtn; // 添加成绩按钮
    @FXML
    private Button deleteBtn; // 删除成绩按钮
    @FXML
    private Button queryBtn; // 查询成绩按钮
    @FXML
    private Button updateBtn; // 修改成绩按钮
    @FXML
    private Button viewBtn; // 查看成绩按钮
    @FXML
    private Button logoutBtn; // 退出登录按钮

    @FXML
    private Button idSort; // 按学号排序按钮
    @FXML
    private Button nameSort; // 按姓名排序按钮
    @FXML
    private Button scoreSort; // 按成绩排序按钮
    @FXML
    private Button orderSort; // 升序/降序切换按钮

    // 成绩数据列表（ObservableList支持UI自动刷新）
    private ObservableList<Score> scoreList;
    // 排序标识：true=升序，false=降序
    private boolean isAsc = true;
    // 当前排序维度：0=学号，1=姓名，2=成绩
    private int currentSortDim = 0;
    // 用于排序
    private SortedList<Score> sortedData;
    // 角色身份
    @Getter
    @Setter
    private static String role;

    /**
     * 初始化方法（JavaFX Initializable接口实现）
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. 初始化表格列与数据模型的映射
        initTableColumns();

        // 2. 初始化空数据列表（移除测试数据）
        initEmptyDataList();

        // 3. 绑定按钮事件
        bindButtonEvents();

        // 4. 初始化搜索功能（实时过滤）
        initSearchFunction();

        // 5. 初始化用户信息
        realname.setText(" 当前登录：管理员");

        // 6. 初始排序：按学号升序
        sortByDim(currentSortDim, isAsc);
    }

    /**
     * 初始化表格列与数据模型的映射（适配Java 23）
     */
    private void initTableColumns() {
        // 绑定列与Score实体类的属性（PropertyValueFactory兼容Java 23）
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        course.setCellValueFactory(new PropertyValueFactory<>("course"));
        score.setCellValueFactory(new PropertyValueFactory<>("score"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        teacher.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));

        // 设置表格列宽度自适应
        scoreTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    /**
     * 初始化空的成绩数据列表（移除所有测试数据）
     */
    private void initEmptyDataList() {
        // 仅初始化空列表，不添加任何测试数据
        scoreList = FXCollections.observableArrayList();
        // 将空列表绑定到表格
        scoreTable.setItems(scoreList);
    }

    /**
     * 绑定所有按钮的点击事件（Java 23语法兼容）
     */
    private void bindButtonEvents() {
        // 功能按钮事件
        addBtn.setOnAction(e -> handleAdd());
        deleteBtn.setOnAction(e -> handleDelete());
        queryBtn.setOnAction(e -> handleQuery());
        updateBtn.setOnAction(e -> handleUpdate());
        viewBtn.setOnAction(e -> handleView());
        logoutBtn.setOnAction(e -> handleLogout());

        // 排序按钮事件
        idSort.setOnAction(e -> {
            currentSortDim = 0;
            sortByDim(currentSortDim, isAsc);
            sortType.setText("    学号");
        });
        nameSort.setOnAction(e -> {
            currentSortDim = 1;
            sortByDim(currentSortDim, isAsc);
            sortType.setText("    姓名");
        });
        scoreSort.setOnAction(e -> {
            currentSortDim = 2;
            sortByDim(currentSortDim, isAsc);
            sortType.setText("    成绩");
        });
        // 升序/降序切换
        orderSort.setOnAction(e -> {
            isAsc = !isAsc;
            orderSort.setText(isAsc ? "升  序" : "降  序");
            sortByDim(currentSortDim, isAsc);
        });
    }

    /**
     * 初始化搜索和排序（确保sortedData被正确初始化并绑定到表格）
     */
    private void initSearchFunction() {
        // 创建过滤列表（基于原始数据）
        FilteredList<Score> filteredData = new FilteredList<>(scoreList, p -> true);

        // 监听搜索框输入变化，实时过滤
        idSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));
        nameSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));
        courseSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));

        // 初始化全局的sortedData（关键：后续排序操作这个对象）
        sortedData = new SortedList<>(filteredData);
        // 绑定排序列表到表格
        scoreTable.setItems(sortedData);
    }

    /**
     * 根据搜索框内容过滤数据（Java 23兼容）
     */
    private void filterData(FilteredList<Score> filteredData) {
        String idKeyword = idSearchInput.getText().trim().toLowerCase();
        String nameKeyword = nameSearchInput.getText().trim().toLowerCase();
        String courseKeyword = courseSearchInput.getText().trim().toLowerCase();

        filteredData.setPredicate(score -> {
            // 所有搜索框为空时，显示全部数据
            if (idKeyword.isEmpty() && nameKeyword.isEmpty() && courseKeyword.isEmpty()) {
                return true;
            }

            // 多条件过滤（交集）
            return score.getUserId().toLowerCase().contains(idKeyword)
                    && score.getUserName().toLowerCase().contains(nameKeyword)
                    && score.getCourseName().toLowerCase().contains(courseKeyword);
        });
    }

    /**
     * 按指定维度排序数据（Java 23的Comparator兼容）
     * @param dim 排序维度：0=学号，1=姓名，2=成绩
     * @param asc 是否升序
     */
    private void sortByDim(int dim, boolean asc) {
        Comparator<Score> comparator = switch (dim) { // Java 14+ switch表达式，兼容Java 23
            case 0 -> Comparator.comparing(Score::getUserId); // 学号
            case 1 -> Comparator.comparing(Score::getUserName); // 姓名
            case 2 -> Comparator.comparing(Score::getScoreValue, // 成绩
                    (bd1, bd2) -> {
                        // 空值处理，避免NullPointerException
                        if (bd1 == null) return bd2 == null ? 0 : -1;
                        if (bd2 == null) return 1;
                        return bd1.compareTo(bd2);
                    });
            default -> Comparator.comparing(Score::getUserId);  // 默认学号
        };

        // 升序/降序切换
        if (!asc) {
            comparator = comparator.reversed();
        }
        sortedData.setComparator(comparator);
    }

    /**
     * 处理添加成绩按钮事件
     */
    private void handleAdd() {
        Util.showAlert(Alert.AlertType.INFORMATION, "提示", "即将打开添加成绩界面！");
        // 实际项目中：对接数据库，新增成绩数据到scoreList
    }

    /**
     * 处理删除成绩按钮事件
     */
    private void handleDelete() {
        // 获取选中的行
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要删除的成绩行！");
            return;
        }

        // 确认删除（Java 23 Alert API无变化）
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要删除【" + selectedScore.getUserName() + "-" + selectedScore.getCourseName() + "】的成绩吗？");
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // 从列表中移除选中数据（后续需对接数据库删除）
                scoreList.remove(selectedScore);
                Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩删除成功！");
            }
        });
    }

    /**
     * 处理查询成绩按钮事件
     */
    private void handleQuery() {
        // 手动触发搜索（无测试数据时，仅执行过滤逻辑）
        FilteredList<Score> filteredData = new FilteredList<>(scoreList, p -> true);
        filterData(filteredData);
        scoreTable.setItems(filteredData);
        Util.showAlert(Alert.AlertType.INFORMATION, "查询结果", "共查询到 " + scoreTable.getItems().size() + " 条成绩数据！");
    }

    /**
     * 处理修改成绩按钮事件
     */
    private void handleUpdate() {
        // 获取选中的行
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要修改的成绩行！");
            return;
        }

        Util.showAlert(Alert.AlertType.INFORMATION, "提示", "即将打开修改成绩界面！\n当前选中：" +
                selectedScore.getUserName() + " - " + selectedScore.getCourseName() + "（原成绩：" + selectedScore.getScoreValue() + "）");
        // 实际项目中：对接数据库，更新选中的成绩数据
    }

    /**
     * 处理查看成绩按钮事件
     */
    private void handleView() {
        // 获取选中的行
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要查看的成绩行！");
            return;
        }

        // 拼接详细信息
        String detail = "成绩详情：\n" +
                "学号：" + selectedScore.getUserId() + "\n" +
                "姓名：" + selectedScore.getUserName() + "\n" +
                "课程：" + selectedScore.getCourseName() + "\n" +
                "成绩：" + selectedScore.getScoreValue() + "\n" +
                "类型：" + selectedScore.getExamType() + "\n" +
                "授课老师：" + selectedScore.getCreateUser() + "\n" +
                "时间：" + selectedScore.getUpdateTime();

        Util.showAlert(Alert.AlertType.INFORMATION, "成绩详情", detail);
    }

    /**
     * 处理退出登录按钮事件
     */
    private void handleLogout() {
        // 确认退出
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认退出");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要退出登录吗？");
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // 切换至登录界面
                switchToLogin();
            }
        });
    }

    private void switchToLogin() {
        try {
            // 获取当前Stage
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/fxml/login.fxml") // 替换成你要跳转的register.fxml
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
                currentStage.setScene(new Scene(newRoot, 360, 500));
            } else {
                currentStage.getScene().setRoot(newRoot);
                currentStage.setWidth(360);
                currentStage.setHeight(500);
                currentStage.centerOnScreen();
            }
            // 设置窗口标题
            currentStage.setTitle("登录");
        } catch (IOException e) {
            e.printStackTrace();
            Util.showAlert(Alert.AlertType.WARNING, "跳转失败", "登录页面加载出错，请重试！");
        }
    }

    // 对外提供设置登录用户姓名的方法
    public void setRealname(String username) {
        realname.setText(" 当前登录：" + username);
    }

    // 对外提供设置成绩数据的方法（用于从数据库加载数据后赋值）
    public void setScoreList(ObservableList<Score> scoreList) {
        this.scoreList = scoreList;
        scoreTable.setItems(scoreList);
        // 重新初始化搜索功能，适配新数据
        initSearchFunction();
    }
}