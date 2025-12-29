package com.example.grademanage.Controller;

import com.example.grademanage.Adapter.*;
import com.example.grademanage.Entity.Score;
import com.example.grademanage.Factory.BeanFactory;
import com.example.grademanage.Factory.FactoryProducer;
import com.example.grademanage.Factory.PermissionFactory;
import com.example.grademanage.Service.ScoreService;
import com.example.grademanage.Service.UserService;
import com.example.grademanage.Util.Util;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

/**
 * 成绩管理控制器
 * 负责成绩的增删改查、筛选、排序等核心功能
 */
public class ViewController implements Initializable {
    // ===================== FXML绑定组件 =====================
    @FXML private Label realname;
    @FXML private TableView<Score> scoreTable;
    @FXML private VBox functionBtnBox;

    @FXML private TableColumn<Score, Integer> check;
    @FXML private TableColumn<Score, String> id;
    @FXML private TableColumn<Score, String> name;
    @FXML private TableColumn<Score, String> course;
    @FXML private TableColumn<Score, Double> score;
    @FXML private TableColumn<Score, String> type;
    @FXML private TableColumn<Score, String> teacher;
    @FXML private TableColumn<Score, String> time;

    @FXML private TextField idSearchInput;
    @FXML private TextField nameSearchInput;
    @FXML private TextField courseSearchInput;
    @FXML private Label sortType;

    @FXML private Button logoutBtn;
    @FXML private Button idSort;
    @FXML private Button nameSort;
    @FXML private Button scoreSort;
    @FXML private Button courseSort;
    @FXML private Button orderSort;

    // ===================== 自定义成员变量 =====================
    private Button addBtn;
    private Button deleteBtn;
    private Button queryBtn;
    private Button updateBtn;
    private Button viewBtn;
    private Button viewAllBtn;

    private ObservableList<Score> scoreList;          // 成绩列表（原始全量数据）
    private boolean isAsc = true;                     // 是否升序排序
    private int currentSortDim = 0;                   // 当前排序维度：0-学号 1-姓名 2-成绩 3-课程
    private Map<Integer, SortAdapter> sortAdapterMap; // 排序适配器映射表
    private FilteredList<Score> filteredData;         // 第一层筛选：查询/显示全部的结果
    private FilteredList<Score> searchFilteredData;   // 第二层筛选：搜索框基于filteredData的筛选
    private SortedList<Score> sortedData;             // 最终排序后的成绩数据
    private Integer roleId;                           // 当前用户角色ID
    private PermissionFactory permissionFactory;      // 权限工厂
    private ScoreService scoreService;                // 成绩服务
    private UserService userService;                  // 用户服务

    // ===================== 初始化方法 =====================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化工厂与服务
        permissionFactory = (PermissionFactory) FactoryProducer.getFactory(FactoryProducer.FactoryType.PERMISSION);
        roleId = Util.getUser().getRoleId();
        scoreList = FXCollections.observableArrayList();

        BeanFactory beanFactory = (BeanFactory) FactoryProducer.getFactory(FactoryProducer.FactoryType.BEAN);
        scoreService = beanFactory.getBean("scoreService");
        userService = beanFactory.getBean("userService");

        // 执行初始化流程
        initSortAdapters();
        initScoreList();
        initTableColumns();
        initUserInfo();
        createDynamicFunctionButtons();
        addDynamicButtonsToContainer();
        bindButtonEvents();
        initSearchFunction();
        sortByDim(currentSortDim, isAsc);
    }

    // ===================== 初始化辅助方法 =====================
    /**
     * 初始化排序适配器映射
     */
    private void initSortAdapters() {
        sortAdapterMap = new HashMap<>();
        sortAdapterMap.put(0, new UserIdSortAdapter());     // 学号适配器
        sortAdapterMap.put(1, new UserNameSortAdapter());   // 姓名适配器
        sortAdapterMap.put(2, new ScoreValueSortAdapter()); // 成绩适配器
        sortAdapterMap.put(3, new CourseNameSortAdapter()); // 课程适配器
    }

    /**
     * 初始化成绩列表（按角色加载对应数据）
     */
    private void initScoreList() {
        List<Score> tempList = switch (roleId) {
            case 1 -> {
                // 学生角色：仅加载自己的成绩
                List<Score> userScores = scoreService.getScoresByUserId(Util.getUser().getUserId());
                yield userScores == null ? new ArrayList<>() : userScores;
            }
            case 2, 3 -> {
                // 教师/辅导员：加载所有成绩
                List<Score> allScores = scoreService.getAllScores();
                yield allScores == null ? new ArrayList<>() : allScores;
            }
            default -> new ArrayList<>();
        };

        scoreList.clear();
        if (!tempList.isEmpty()) {
            scoreList.addAll(tempList);
        }
    }

    /**
     * 初始化空数据列表（预留方法）
     */
    private void initEmptyDataList() {
        scoreList = FXCollections.observableArrayList();
        scoreTable.setItems(scoreList);
    }

    /**
     * 初始化用户信息（显示登录角色和名称）
     */
    private void initUserInfo() {
        String roleName = switch (roleId) {
            case 1 -> "学生";
            case 2 -> "教师";
            case 3 -> "辅导员";
            default -> throw new IllegalStateException("Unexpected roleId: " + roleId);
        };
        realname.setText(" 当前登录：" + roleName + "  " + Util.getUser().getRealName());
    }

    /**
     * 动态创建功能按钮
     */
    private void createDynamicFunctionButtons() {
        addBtn = new Button("添加");
        deleteBtn = new Button("删除");
        queryBtn = new Button("查询");
        updateBtn = new Button("修改");
        viewBtn = new Button("查看");
        viewAllBtn = new Button("显示全部");

        setFunctionButtonStyle(addBtn, deleteBtn, queryBtn, updateBtn, viewBtn, viewAllBtn);
        viewAllBtn.setFont(Font.font(20));
    }

    /**
     * 统一设置功能按钮样式
     */
    private void setFunctionButtonStyle(Button... buttons) {
        for (Button btn : buttons) {
            btn.setPrefWidth(145);
            btn.setPrefHeight(42);
            btn.setFont(Font.font(24));
        }
    }

    /**
     * 按权限将按钮添加到布局容器
     */
    private void addDynamicButtonsToContainer() {
        if (hasPermission("save")) {
            functionBtnBox.getChildren().add(addBtn);
        }
        if (hasPermission("delete")) {
            functionBtnBox.getChildren().add(deleteBtn);
        }
        if (hasPermission("query")) {
            functionBtnBox.getChildren().add(queryBtn);
        }
        if (hasPermission("update")) {
            functionBtnBox.getChildren().add(updateBtn);
        }
        if (hasPermission("view")) {
            functionBtnBox.getChildren().add(viewBtn);
        }
        functionBtnBox.getChildren().add(viewAllBtn);
    }

    /**
     * 初始化表格列配置
     */
    private void initTableColumns() {
        // 序号列配置
        check.setReorderable(false);
        check.setCellFactory(col -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                    setFont(Font.font(14));
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        check.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(scoreTable.getItems().indexOf(cellData.getValue())).asObject()
        );

        // 数据列配置
        id.setReorderable(false);
        id.setCellValueFactory(new PropertyValueFactory<>("userId"));

        name.setReorderable(false);
        name.setCellValueFactory(new PropertyValueFactory<>("userName"));

        course.setReorderable(false);
        course.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        score.setReorderable(false);
        score.setCellValueFactory(new PropertyValueFactory<>("scoreValue"));

        type.setReorderable(false);
        type.setCellValueFactory(new PropertyValueFactory<>("examType"));

        teacher.setReorderable(false);
        teacher.setCellValueFactory(new PropertyValueFactory<>("createUser"));

        time.setReorderable(false);
        time.setCellValueFactory(new PropertyValueFactory<>("createTime"));

        // 表格样式配置
        scoreTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        check.setPrefWidth(28);
    }

    /**
     * 初始化搜索功能（分层筛选）
     */
    private void initSearchFunction() {
        // 第一层筛选：初始为全量数据
        filteredData = new FilteredList<>(scoreList, p -> true);
        // 第二层筛选：搜索框基于第一层结果筛选
        searchFilteredData = new FilteredList<>(filteredData, p -> true);

        // 绑定搜索框监听
        idSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        nameSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        courseSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());

        // 排序基于第二层筛选结果
        sortedData = new SortedList<>(searchFilteredData);
        scoreTable.setItems(sortedData);
    }

    // ===================== 事件绑定与处理 =====================
    /**
     * 绑定所有按钮事件
     */
    private void bindButtonEvents() {
        // 功能按钮事件
        if (addBtn != null) addBtn.setOnAction(e -> handleAdd());
        if (deleteBtn != null) deleteBtn.setOnAction(e -> handleDelete());
        if (queryBtn != null) queryBtn.setOnAction(e -> handleQuery());
        if (updateBtn != null) updateBtn.setOnAction(e -> handleUpdate());
        if (viewBtn != null) viewBtn.setOnAction(e -> handleView());
        if (viewAllBtn != null) viewAllBtn.setOnAction(e -> handleViewAll());

        // 排序按钮事件
        logoutBtn.setOnAction(e -> handleLogout());

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

        courseSort.setOnAction(e -> {
            currentSortDim = 3;
            sortByDim(currentSortDim, isAsc);
            sortType.setText("    课程");
        });

        orderSort.setOnAction(e -> {
            isAsc = !isAsc;
            orderSort.setText(isAsc ? "升  序" : "降  序");
            sortByDim(currentSortDim, isAsc);
        });
    }

    /**
     * 新增成绩处理
     */
    private void handleAdd() {
        // 创建弹窗
        Stage addScoreStage = new Stage();
        addScoreStage.setTitle("新增成绩");
        addScoreStage.setWidth(500);
        addScoreStage.setHeight(680);
        addScoreStage.setResizable(false);
        addScoreStage.initModality(Modality.APPLICATION_MODAL);
        addScoreStage.initOwner(addBtn.getScene().getWindow());

        // 表单容器
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new javafx.geometry.Insets(30, 30, 30, 30));

        // 表单控件
        Label userIdLabel = new Label("学生ID *");
        userIdLabel.setFont(Font.font(16));
        TextField userIdField = new TextField();
        userIdField.setPrefWidth(300);
        userIdField.setFont(Font.font(16));

        Label userNameLabel = new Label("学生姓名 *");
        userNameLabel.setFont(Font.font(16));
        TextField userNameField = new TextField();
        userNameField.setPrefWidth(300);
        userNameField.setFont(Font.font(16));

        Label courseNameLabel = new Label("课程名称 *");
        courseNameLabel.setFont(Font.font(16));
        TextField courseNameField = new TextField();
        courseNameField.setPrefWidth(300);
        courseNameField.setFont(Font.font(16));

        Label scoreValueLabel = new Label("成绩（0-100） *");
        scoreValueLabel.setFont(Font.font(15));
        TextField scoreValueField = new TextField();
        scoreValueField.setPrefWidth(300);
        scoreValueField.setFont(Font.font(16));

        Label examTypeLabel = new Label("考试类型");
        examTypeLabel.setFont(Font.font(16));
        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.setPrefWidth(300);
        examTypeBox.getItems().addAll("期中", "期末", "补考", "其他");
        examTypeBox.setValue("其他");

        Label teacherIdLabel = new Label("教师ID *");
        teacherIdLabel.setFont(Font.font(16));
        TextField teacherIdField = new TextField();
        teacherIdField.setPrefWidth(300);
        teacherIdField.setFont(Font.font(16));
        if (roleId == 2) {
            teacherIdField.setText(Util.getUser().getUserId());
            teacherIdField.setEditable(false);
        }

        Label teacherNameLabel = new Label("教师名称 *");
        teacherNameLabel.setFont(Font.font(16));
        TextField teacherNameField = new TextField();
        teacherNameField.setPrefWidth(300);
        teacherNameField.setFont(Font.font(16));
        if (roleId == 2) {
            teacherNameField.setText(Util.getUser().getRealName());
            teacherNameField.setEditable(false);
        }

        Button submitBtn = new Button("提交");
        submitBtn.setPrefWidth(120);
        submitBtn.setPrefHeight(40);
        submitBtn.setFont(Font.font(18));

        Button cancelBtn = new Button("取消");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setFont(Font.font(18));

        // 布局控件
        gridPane.add(userIdLabel, 0, 0);
        gridPane.add(userIdField, 1, 0);
        gridPane.add(userNameLabel, 0, 1);
        gridPane.add(userNameField, 1, 1);
        gridPane.add(courseNameLabel, 0, 2);
        gridPane.add(courseNameField, 1, 2);
        gridPane.add(scoreValueLabel, 0, 3);
        gridPane.add(scoreValueField, 1, 3);
        gridPane.add(examTypeLabel, 0, 4);
        gridPane.add(examTypeBox, 1, 4);
        gridPane.add(teacherIdLabel, 0, 5);
        gridPane.add(teacherIdField, 1, 5);
        gridPane.add(teacherNameLabel, 0, 6);
        gridPane.add(teacherNameField, 1, 6);
        gridPane.add(submitBtn, 0, 7);
        gridPane.add(cancelBtn, 1, 7);

        // 场景与事件
        Scene scene = new Scene(gridPane);
        addScoreStage.setScene(scene);

        cancelBtn.setOnAction(e -> addScoreStage.close());

        submitBtn.setOnAction(e -> {
            // 非空校验
            if (userIdField.getText().trim().isEmpty() ||
                    userNameField.getText().trim().isEmpty() ||
                    courseNameField.getText().trim().isEmpty() ||
                    scoreValueField.getText().trim().isEmpty() ||
                    teacherIdField.getText().trim().isEmpty() ||
                    teacherNameField.getText().trim().isEmpty()) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "信息填写不完整！");
                return;
            }

            // 学号姓名校验
            if (userService.getUserById(userIdField.getText()) == null) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "该学生不存在！");
                return;
            }
            if (!Objects.equals(getNameByUserId(userIdField.getText()), userNameField.getText())) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "学号与姓名不符！");
                return;
            }

            // 成绩格式校验
            double scoreValue;
            try {
                scoreValue = Double.parseDouble(scoreValueField.getText().trim());
                if (scoreValue < 0 || scoreValue > 100) {
                    Util.showAlert(Alert.AlertType.ERROR, "错误", "成绩必须在0-100之间！");
                    return;
                }
            } catch (NumberFormatException ex) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "成绩必须是数字格式！");
                return;
            }

            // 封装数据
            Score newScore = new Score();
            newScore.setUserId(userIdField.getText().trim());
            newScore.setUserName(userNameField.getText().trim());
            newScore.setCourseName(courseNameField.getText().trim());
            newScore.setScoreValue(BigDecimal.valueOf(scoreValue));
            newScore.setExamType(examTypeBox.getValue());
            newScore.setCreateUser(teacherIdField.getText().trim());

            Timestamp now = new Timestamp(System.currentTimeMillis());
            newScore.setCreateTime(now);
            newScore.setUpdateTime(now);

            // 保存数据
            scoreList.add(newScore);
            scoreService.saveScore(newScore);
            initScoreList();

            Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩新增成功！");
            addScoreStage.close();
        });

        addScoreStage.showAndWait();
    }

    /**
     * 删除成绩处理
     */
    private void handleDelete() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要删除的成绩行！");
            return;
        }

        // 确认删除
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要删除【" + selectedScore.getUserName() + "-" + selectedScore.getCourseName() + "】的成绩吗？");

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                scoreList.remove(selectedScore);
                scoreService.deleteScore(selectedScore.getScoreId());
                initScoreList();
                Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩删除成功！");
            }
        });
    }

    /**
     * 查询成绩处理
     */
    private void handleQuery() {
        // 创建查询窗口
        Stage queryScoreStage = new Stage();
        queryScoreStage.setTitle("成绩查询");
        queryScoreStage.setWidth(500);
        queryScoreStage.setHeight(500);
        queryScoreStage.setResizable(false);
        queryScoreStage.initModality(Modality.APPLICATION_MODAL);
        queryScoreStage.initOwner(scoreTable.getScene().getWindow());

        // 表单容器
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new javafx.geometry.Insets(30, 30, 30, 30));

        // 表单控件
        Label userIdLabel = new Label("学生ID");
        userIdLabel.setFont(Font.font(16));
        TextField userIdField = new TextField();
        userIdField.setPrefWidth(300);
        userIdField.setFont(Font.font(16));
        userIdField.setPromptText("支持模糊查询，留空则不限制");

        Label userNameLabel = new Label("学生姓名");
        userNameLabel.setFont(Font.font(16));
        TextField userNameField = new TextField();
        userNameField.setPrefWidth(300);
        userNameField.setFont(Font.font(16));
        userNameField.setPromptText("支持模糊查询，留空则不限制");

        Label courseNameLabel = new Label("课程名称");
        courseNameLabel.setFont(Font.font(16));
        TextField courseNameField = new TextField();
        courseNameField.setPrefWidth(300);
        courseNameField.setFont(Font.font(16));
        courseNameField.setPromptText("支持模糊查询，留空则不限制");

        Label examTypeLabel = new Label("考试类型");
        examTypeLabel.setFont(Font.font(16));
        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.setPrefWidth(300);
        examTypeBox.getItems().addAll("", "期中", "期末", "补考", "其他");
        examTypeBox.setValue("");
        examTypeBox.setPromptText("选择考试类型，留空则不限制");

        // 成绩范围控件
        Label scoreRangeLabel = new Label("成绩范围");
        scoreRangeLabel.setFont(Font.font(16));
        GridPane scoreRangeGrid = new GridPane();
        scoreRangeGrid.setHgap(10);

        TextField scoreMinField = new TextField();
        scoreMinField.setPrefWidth(145);
        scoreMinField.setFont(Font.font(16));
        scoreMinField.setPromptText("最低分（0-100）");

        TextField scoreMaxField = new TextField();
        scoreMaxField.setPrefWidth(145);
        scoreMaxField.setFont(Font.font(16));
        scoreMaxField.setPromptText("最高分（0-100）");

        scoreRangeGrid.add(new Label("≥"), 0, 0);
        scoreRangeGrid.add(scoreMinField, 1, 0);
        scoreRangeGrid.add(new Label("≤"), 2, 0);
        scoreRangeGrid.add(scoreMaxField, 3, 0);

        // 按钮控件
        Button queryBtn = new Button("查询");
        queryBtn.setPrefWidth(120);
        queryBtn.setPrefHeight(40);
        queryBtn.setFont(Font.font(18));
        queryBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button resetBtn = new Button("重置");
        resetBtn.setPrefWidth(120);
        resetBtn.setPrefHeight(40);
        resetBtn.setFont(Font.font(18));
        resetBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // 布局控件
        gridPane.add(userIdLabel, 0, 0);
        gridPane.add(userIdField, 1, 0);
        gridPane.add(userNameLabel, 0, 1);
        gridPane.add(userNameField, 1, 1);
        gridPane.add(courseNameLabel, 0, 2);
        gridPane.add(courseNameField, 1, 2);
        gridPane.add(examTypeLabel, 0, 3);
        gridPane.add(examTypeBox, 1, 3);
        gridPane.add(scoreRangeLabel, 0, 4);
        gridPane.add(scoreRangeGrid, 1, 4);
        gridPane.add(queryBtn, 0, 5);
        gridPane.add(resetBtn, 1, 5);

        // 场景与事件
        Scene scene = new Scene(gridPane);
        queryScoreStage.setScene(scene);

        // 重置按钮事件
        resetBtn.setOnAction(e -> {
            userIdField.clear();
            userNameField.clear();
            courseNameField.clear();
            examTypeBox.setValue("");
            scoreMinField.clear();
            scoreMaxField.clear();
            queryScoreStage.close();
        });

        // 查询按钮事件
        queryBtn.setOnAction(e -> {
            // 获取查询条件
            String idKeyword = userIdField.getText().trim().toLowerCase();
            String nameKeyword = userNameField.getText().trim().toLowerCase();
            String courseKeyword = courseNameField.getText().trim().toLowerCase();
            String examType = examTypeBox.getValue().trim();
            String scoreMinStr = scoreMinField.getText().trim();
            String scoreMaxStr = scoreMaxField.getText().trim();

            // 成绩范围校验
            Double scoreMin = null;
            Double scoreMax = null;
            try {
                if (!scoreMinStr.isEmpty()) {
                    scoreMin = Double.parseDouble(scoreMinStr);
                    if (scoreMin < 0 || scoreMin > 100) {
                        Util.showAlert(Alert.AlertType.ERROR, "错误", "最低分必须在0-100之间！");
                        return;
                    }
                }
                if (!scoreMaxStr.isEmpty()) {
                    scoreMax = Double.parseDouble(scoreMaxStr);
                    if (scoreMax < 0 || scoreMax > 100) {
                        Util.showAlert(Alert.AlertType.ERROR, "错误", "最高分必须在0-100之间！");
                        return;
                    }
                }
                if (scoreMin != null && scoreMax != null && scoreMin > scoreMax) {
                    Util.showAlert(Alert.AlertType.ERROR, "错误", "最低分不能大于最高分！");
                    return;
                }
            } catch (NumberFormatException ex) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "成绩必须是数字格式！");
                return;
            }

            // 构建筛选条件
            Double finalScoreMax = scoreMax;
            Double finalScoreMin = scoreMin;
            filteredData = new FilteredList<>(scoreList, score -> {
                boolean idMatch = idKeyword.isEmpty() || score.getUserId().toLowerCase().contains(idKeyword);
                boolean nameMatch = nameKeyword.isEmpty() || score.getUserName().toLowerCase().contains(nameKeyword);
                boolean courseMatch = courseKeyword.isEmpty() || score.getCourseName().toLowerCase().contains(courseKeyword);
                boolean examTypeMatch = examType.isEmpty() || score.getExamType().equals(examType);

                boolean scoreMatch = true;
                if (score.getScoreValue() != null) {
                    double scoreVal = score.getScoreValue().doubleValue();
                    if (finalScoreMin != null) scoreMatch = scoreVal >= finalScoreMin;
                    if (finalScoreMax != null) scoreMatch = scoreMatch && scoreVal <= finalScoreMax;
                }

                return idMatch && nameMatch && courseMatch && examTypeMatch && scoreMatch;
            });

            // 重置搜索筛选并重新绑定监听
            searchFilteredData = new FilteredList<>(filteredData, p -> true);
            idSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
            nameSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
            courseSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
            filterData();

            // 更新排序和表格数据
            sortedData = new SortedList<>(searchFilteredData);
            sortByDim(currentSortDim, isAsc);
            scoreTable.setItems(sortedData);

            // 提示结果
            Util.showAlert(Alert.AlertType.INFORMATION, "查询结果",
                    "共查询到 " + filteredData.size() + " 条成绩数据！");
            queryScoreStage.close();
        });

        queryScoreStage.showAndWait();
    }

    /**
     * 修改成绩处理
     */
    private void handleUpdate() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要修改的成绩行！");
            return;
        }

        // 创建修改窗口
        Stage updateScoreStage = new Stage();
        updateScoreStage.setTitle("修改成绩");
        updateScoreStage.setWidth(500);
        updateScoreStage.setHeight(680);
        updateScoreStage.setResizable(false);
        updateScoreStage.initModality(Modality.APPLICATION_MODAL);
        updateScoreStage.initOwner(scoreTable.getScene().getWindow());

        // 表单容器
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new javafx.geometry.Insets(30, 30, 30, 30));

        // 表单控件（预填充数据）
        Label userIdLabel = new Label("学生ID *");
        userIdLabel.setFont(Font.font(16));
        TextField userIdField = new TextField();
        userIdField.setPrefWidth(300);
        userIdField.setFont(Font.font(16));
        userIdField.setText(selectedScore.getUserId());
        userIdField.setEditable(false);

        Label userNameLabel = new Label("学生姓名 *");
        userNameLabel.setFont(Font.font(16));
        TextField userNameField = new TextField();
        userNameField.setPrefWidth(300);
        userNameField.setFont(Font.font(16));
        userNameField.setText(selectedScore.getUserName());

        Label courseNameLabel = new Label("课程名称 *");
        courseNameLabel.setFont(Font.font(16));
        TextField courseNameField = new TextField();
        courseNameField.setPrefWidth(300);
        courseNameField.setFont(Font.font(16));
        courseNameField.setText(selectedScore.getCourseName());

        Label scoreValueLabel = new Label("成绩（0-100） *");
        scoreValueLabel.setFont(Font.font(15));
        TextField scoreValueField = new TextField();
        scoreValueField.setPrefWidth(300);
        scoreValueField.setFont(Font.font(16));
        scoreValueField.setText(selectedScore.getScoreValue() != null ?
                selectedScore.getScoreValue().toString() : "0");

        Label examTypeLabel = new Label("考试类型");
        examTypeLabel.setFont(Font.font(16));
        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.setPrefWidth(300);
        examTypeBox.getItems().addAll("期中", "期末", "补考", "其他");
        examTypeBox.setValue(selectedScore.getExamType() != null ?
                selectedScore.getExamType() : "其他");

        Label teacherIdLabel = new Label("教师ID *");
        teacherIdLabel.setFont(Font.font(16));
        TextField teacherIdField = new TextField();
        teacherIdField.setPrefWidth(300);
        teacherIdField.setFont(Font.font(16));
        teacherIdField.setText(selectedScore.getCreateUser());
        if (roleId == 2) {
            teacherIdField.setText(Util.getUser().getUserId());
            teacherIdField.setEditable(false);
        }

        Label teacherNameLabel = new Label("教师名称 *");
        teacherNameLabel.setFont(Font.font(16));
        TextField teacherNameField = new TextField();
        teacherNameField.setPrefWidth(300);
        teacherNameField.setFont(Font.font(16));
        String teacherName = userService.getUserById(teacherIdField.getText()).getRealName();
        teacherNameField.setText(teacherName);
        if (roleId == 2) {
            teacherNameField.setText(Util.getUser().getRealName());
            teacherNameField.setEditable(false);
        }

        Button submitBtn = new Button("提交");
        submitBtn.setPrefWidth(120);
        submitBtn.setPrefHeight(40);
        submitBtn.setFont(Font.font(18));

        Button cancelBtn = new Button("取消");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setFont(Font.font(18));

        // 布局控件
        gridPane.add(userIdLabel, 0, 0);
        gridPane.add(userIdField, 1, 0);
        gridPane.add(userNameLabel, 0, 1);
        gridPane.add(userNameField, 1, 1);
        gridPane.add(courseNameLabel, 0, 2);
        gridPane.add(courseNameField, 1, 2);
        gridPane.add(scoreValueLabel, 0, 3);
        gridPane.add(scoreValueField, 1, 3);
        gridPane.add(examTypeLabel, 0, 4);
        gridPane.add(examTypeBox, 1, 4);
        gridPane.add(teacherIdLabel, 0, 5);
        gridPane.add(teacherIdField, 1, 5);
        gridPane.add(teacherNameLabel, 0, 6);
        gridPane.add(teacherNameField, 1, 6);
        gridPane.add(submitBtn, 0, 7);
        gridPane.add(cancelBtn, 1, 7);

        // 场景与事件
        Scene scene = new Scene(gridPane);
        updateScoreStage.setScene(scene);

        cancelBtn.setOnAction(e -> updateScoreStage.close());

        submitBtn.setOnAction(e -> {
            // 非空校验
            if (userNameField.getText().trim().isEmpty() ||
                    courseNameField.getText().trim().isEmpty() ||
                    scoreValueField.getText().trim().isEmpty() ||
                    teacherIdField.getText().trim().isEmpty() ||
                    teacherNameField.getText().trim().isEmpty()) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "信息填写不完整！");
                return;
            }

            // 学号姓名校验
            if (!Objects.equals(getNameByUserId(userIdField.getText()), userNameField.getText())) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "学号与姓名不符！");
                return;
            }

            // 成绩格式校验
            double scoreValue;
            try {
                scoreValue = Double.parseDouble(scoreValueField.getText().trim());
                if (scoreValue < 0 || scoreValue > 100) {
                    Util.showAlert(Alert.AlertType.ERROR, "错误", "成绩必须在0-100之间！");
                    return;
                }
            } catch (NumberFormatException ex) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "成绩必须是数字格式！");
                return;
            }

            // 更新数据
            selectedScore.setUserName(userNameField.getText().trim());
            selectedScore.setCourseName(courseNameField.getText().trim());
            selectedScore.setScoreValue(BigDecimal.valueOf(scoreValue));
            selectedScore.setExamType(examTypeBox.getValue());
            selectedScore.setCreateUser(teacherIdField.getText().trim());
            selectedScore.setUpdateTime(new Timestamp(System.currentTimeMillis()));

            // 保存修改
            scoreService.updateScore(selectedScore);
            scoreTable.refresh();

            Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩修改成功！");
            updateScoreStage.close();
        });

        updateScoreStage.showAndWait();
    }

    /**
     * 显示全部成绩处理
     */
    private void handleViewAll() {
        // 清空搜索框
        idSearchInput.clear();
        nameSearchInput.clear();
        courseSearchInput.clear();

        // 重置筛选层级
        filteredData = new FilteredList<>(scoreList, p -> true);
        searchFilteredData = new FilteredList<>(filteredData, p -> true);

        // 重新绑定搜索监听
        idSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        nameSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        courseSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData());

        // 执行空筛选
        filterData();

        // 更新排序和表格
        sortedData = new SortedList<>(searchFilteredData);
        sortByDim(currentSortDim, isAsc);
        scoreTable.setItems(sortedData);
        scoreTable.refresh();

        // 提示信息
        Util.showAlert(Alert.AlertType.INFORMATION, "提示",
                "已显示全部成绩数据，共 " + scoreList.size() + " 条！");
    }

    /**
     * 查看成绩详情处理
     */
    private void handleView() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要查看的成绩行！");
            return;
        }

        // 拼接详情信息
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
     * 退出登录处理
     */
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认退出");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要退出登录吗？");

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                switchToLogin();
            }
        });
    }

    // ===================== 通用工具方法 =====================
    /**
     * 切换到登录页面
     */
    private void switchToLogin() {
        try {
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/fxml/login.fxml"));

            if (loader.getLocation() == null) {
                Util.showAlert(Alert.AlertType.ERROR, "路径错误", "打包后未找到/fxml/login.fxml文件，请检查resources目录结构！");
                return;
            }

            Parent newRoot = loader.load();

            if (currentStage.getScene() == null) {
                currentStage.setScene(new Scene(newRoot, 360, 500));
            } else {
                currentStage.getScene().setRoot(newRoot);
                currentStage.setWidth(360);
                currentStage.setHeight(500);
                currentStage.centerOnScreen();
            }

            currentStage.setTitle("登录");
        } catch (IOException e) {
            e.printStackTrace();
            Util.showAlert(Alert.AlertType.WARNING, "跳转失败", "登录页面加载出错，请重试！");
        }
    }

    /**
     * 权限校验封装
     * @param permissionCode 权限编码
     * @return 是否拥有该权限
     */
    private boolean hasPermission(String permissionCode) {
        if (permissionCode == null || permissionCode.isEmpty() || roleId == null) {
            return false;
        }
        return permissionFactory.checkPermission(roleId, permissionCode);
    }

    /**
     * 搜索框筛选逻辑
     */
    private void filterData() {
        String idKeyword = idSearchInput.getText().trim().toLowerCase();
        String nameKeyword = nameSearchInput.getText().trim().toLowerCase();
        String courseKeyword = courseSearchInput.getText().trim().toLowerCase();

        searchFilteredData.setPredicate(score -> {
            if (score == null) {
                return false;
            }

            // 空关键词匹配所有
            if (idKeyword.isEmpty() && nameKeyword.isEmpty() && courseKeyword.isEmpty()) {
                return true;
            }

            // 分别匹配学号、姓名、课程名（模糊匹配）
            String userId = score.getUserId() == null ? "" : score.getUserId().toLowerCase();
            String userName = score.getUserName() == null ? "" : score.getUserName().toLowerCase();
            String courseName = score.getCourseName() == null ? "" : score.getCourseName().toLowerCase();

            boolean idMatch = userId.contains(idKeyword);
            boolean nameMatch = userName.contains(nameKeyword);
            boolean courseMatch = courseName.contains(courseKeyword);

            // 三个条件满足其一即可
            return idMatch && nameMatch && courseMatch;
        });
    }

    /**
     * 按维度排序
     * @param dim 排序维度：0-学号 1-姓名 2-成绩
     * @param asc 是否升序
     */
    private void sortByDim(int dim, boolean asc) {
        if (sortedData == null) return;
        // 通过适配器获取Comparator
        SortAdapter adapter = sortAdapterMap.getOrDefault(dim, new UserIdSortAdapter());
        sortedData.setComparator(adapter.getComparator(asc));
    }

    /**
     * 外部设置成绩列表（预留接口）
     * @param scoreList 成绩列表
     */
    public void setScoreList(ObservableList<Score> scoreList) {
        this.scoreList = scoreList == null ? FXCollections.observableArrayList() : scoreList;
        scoreTable.setItems(this.scoreList);
        initSearchFunction();
    }

    /**
     * 根据用户ID获取用户名
     * @param userId 用户ID
     * @return 用户名
     */
    private String getNameByUserId(String userId) {
        return userService.getUserById(userId).getRealName();
    }
}