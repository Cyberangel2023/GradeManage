package com.example.grademanage.Controller;

import com.example.grademanage.Factory.BeanFactory;
import com.example.grademanage.Factory.FactoryProducer;
import com.example.grademanage.Factory.PermissionFactory;
import com.example.grademanage.Entity.Score;
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
 */
public class ViewController implements Initializable {
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
    @FXML private Button orderSort;

    private Button addBtn;
    private Button deleteBtn;
    private Button queryBtn;
    private Button updateBtn;
    private Button viewBtn;

    private ObservableList<Score> scoreList;       // 成绩列表
    private boolean isAsc = true;                  // 是否升序
    private int currentSortDim = 0;                // 当前排序维度
    private SortedList<Score> sortedData;          // 排序后的成绩
    private Integer roleId;                        // 角色ID
    private PermissionFactory permissionFactory;   // 权限工厂
    private ScoreService scoreService;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 初始化权限工厂
        permissionFactory = (PermissionFactory) FactoryProducer.getFactory(FactoryProducer.FactoryType.PERMISSION);
        roleId = Util.getUser().getRoleId();
        scoreList = FXCollections.observableArrayList();

        BeanFactory beanFactory = (BeanFactory) FactoryProducer.getFactory(FactoryProducer.FactoryType.BEAN);
        scoreService = beanFactory.getBean("scoreService");
        userService = beanFactory.getBean("userService");

        // 初始化逻辑
        initScoreList();                 // 加载成绩数据
        initTableColumns();              // 初始化表格标签
//        initEmptyDataList();             // 初始化数据列表
        initUserInfo();                  // 初始化角色信息标签
        createDynamicFunctionButtons();  // 创建功能按钮
        addDynamicButtonsToContainer();  // 将有权限的按钮添加到布局
        bindButtonEvents();              // 绑定所有按钮事件
        initSearchFunction();            // 初始化搜索功能
        sortByDim(currentSortDim, isAsc);// 排序设置
    }

    private void initScoreList() {
        List<Score> tempList = switch (roleId) {
            case 1 -> {
                // 学生角色：加载自己的成绩
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

        scoreList.clear(); // 清空旧数据
        if (!tempList.isEmpty()) {
            scoreList.addAll(tempList); // 批量添加查询到的成绩数据
        }
    }

    private void initEmptyDataList() {
        scoreList = FXCollections.observableArrayList();
        scoreTable.setItems(scoreList);
    }

    private void initUserInfo() {
        String roleName = switch (roleId) {
            case 1 -> "学生";
            case 2 -> "教师";
            case 3 -> "辅导员";
            default -> throw new IllegalStateException("Unexpected value: " + roleId);
        };
        realname.setText(" 当前登录：" + roleName + "  " + Util.getUser().getRealName());
    }

    /**
     * 动态创建5个功能按钮
     */
    private void createDynamicFunctionButtons() {
        addBtn = new Button("添加");
        deleteBtn = new Button("删除");
        queryBtn = new Button("查询");
        updateBtn = new Button("修改");
        viewBtn = new Button("查看");

        setFunctionButtonStyle(addBtn, deleteBtn, queryBtn, updateBtn, viewBtn);
    }

    /**
     * 统一设置功能按钮的样式
     */
    private void setFunctionButtonStyle(Button... buttons) {
        for (Button btn : buttons) {
            btn.setPrefWidth(145);
            btn.setPrefHeight(42);
            btn.setFont(Font.font(24));
        }
    }

    /**
     * 将有权限的动态按钮添加到FXML布局容器（HBox）
     */
    private void addDynamicButtonsToContainer() {
        // 按权限判断，有权限才添加到布局
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
    }

    // 权限校验封装
    private boolean hasPermission(String permissionCode) {
        if (permissionCode == null || permissionCode.isEmpty() || roleId == null) {
            return false;
        }
        return permissionFactory.checkPermission(roleId, permissionCode);
    }

    private void initTableColumns() {
        // 序号列：自定义渲染行号
        check.setReorderable(false);
        // 自定义单元格渲染，生成连续序号
        check.setCellFactory(col -> new TableCell<Score, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    // 空行显示空字符串
                    setText(null);
                } else {
                    // 行号 = 表格行索引 + 1（自动排序）
                    int rowIndex = getIndex() + 1;
                    setText(String.valueOf(rowIndex));
                    setFont(Font.font(14));
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        // 序号列值工厂
        check.setCellValueFactory(cellData -> new SimpleIntegerProperty(scoreTable.getItems().indexOf(cellData.getValue())).asObject());

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
        scoreTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        check.setPrefWidth(28);
    }

    private void bindButtonEvents() {
        // 动态按钮绑定事件
        if (addBtn != null) {
            addBtn.setOnAction(e -> handleAdd());
        }
        if (deleteBtn != null) {
            deleteBtn.setOnAction(e -> handleDelete());
        }
        if (queryBtn != null) {
            queryBtn.setOnAction(e -> handleQuery());
        }
        if (updateBtn != null) {
            updateBtn.setOnAction(e -> handleUpdate());
        }
        if (viewBtn != null) {
            viewBtn.setOnAction(e -> handleView());
        }

        // 其余按钮事件绑定不变（保留@FXML的按钮）
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
        orderSort.setOnAction(e -> {
            isAsc = !isAsc;
            orderSort.setText(isAsc ? "升  序" : "降  序");
            sortByDim(currentSortDim, isAsc);
        });
    }

    private void initSearchFunction() {
        // 基于已加载的scoreList创建筛选列表
        FilteredList<Score> filteredData = new FilteredList<>(scoreList, p -> true);

        // 绑定搜索框监听
        idSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));
        nameSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));
        courseSearchInput.textProperty().addListener((obs, oldVal, newVal) -> filterData(filteredData));

        // 初始化排序列表并绑定到表格
        sortedData = new SortedList<>(filteredData);
        scoreTable.setItems(sortedData); // 表格最终绑定排序后的列表
    }

    // 筛选诗句
    private void filterData(FilteredList<Score> filteredData) {
        // 获取三个搜索框的关键词，去除首尾空格并转为小写（忽略大小写筛选）
        String idKeyword = idSearchInput.getText().trim().toLowerCase();
        String nameKeyword = nameSearchInput.getText().trim().toLowerCase();
        String courseKeyword = courseSearchInput.getText().trim().toLowerCase();

        // 设置筛选规则：针对Score的特定属性（对应表格特定列）进行匹配
        filteredData.setPredicate(score -> {
            // 空值保护：避免score为null或其属性为null导致空指针异常
            if (score == null) {
                return false;
            }
            // 获取Score对象对应表格列的属性值
            String userId = score.getUserId() == null ? "" : score.getUserId().toLowerCase();
            String userName = score.getUserName() == null ? "" : score.getUserName().toLowerCase();
            String courseName = score.getCourseName() == null ? "" : score.getCourseName().toLowerCase();

            // 多条件叠加筛选：三个关键词同时满足（逻辑与），实现精准列筛选
            // 学号列匹配：包含学号关键词（若关键词为空，则默认匹配成功）
            boolean idMatch = userId.contains(idKeyword);
            // 姓名列匹配：包含姓名关键词（若关键词为空，则默认匹配成功）
            boolean nameMatch = userName.contains(nameKeyword);
            // 课程名列匹配：包含课程名关键词（若关键词为空，则默认匹配成功）
            boolean courseMatch = courseName.contains(courseKeyword);

            // 三个列的匹配条件同时成立，才会保留该条数据
            return idMatch && nameMatch && courseMatch;
        });
    }

    // 排序维度设置
    private void sortByDim(int dim, boolean asc) {
        Comparator<Score> comparator = switch (dim) {
            case 0 -> Comparator.comparing(Score::getUserId);
            case 1 -> Comparator.comparing(Score::getUserName);
            case 2 -> Comparator.comparing(Score::getScoreValue, (bd1, bd2) -> {
                if (bd1 == null) return bd2 == null ? 0 : -1;
                if (bd2 == null) return 1;
                return bd1.compareTo(bd2);
            });
            default -> Comparator.comparing(Score::getUserId);
        };

        if (!asc) {
            comparator = comparator.reversed();
        }
        sortedData.setComparator(comparator);
    }

    private void handleAdd() {
        // 创建弹出窗口
        Stage addScoreStage = new Stage();
        addScoreStage.setTitle("新增成绩");
        addScoreStage.setWidth(500);
        addScoreStage.setHeight(680);
        addScoreStage.setResizable(false);
        addScoreStage.initModality(Modality.APPLICATION_MODAL); // 应用级模态
        addScoreStage.initOwner((addBtn).getScene().getWindow()); // 设置父窗口

        // 创建表单容器
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); // 水平间距
        gridPane.setVgap(15); // 垂直间距
        gridPane.setPadding(new javafx.geometry.Insets(30, 30, 30, 30)); // 内边距

        // 创建表单控件
        // 学生ID
        Label userIdLabel = new Label("学生ID *");
        userIdLabel.setFont(Font.font(16));
        TextField userIdField = new TextField();
        userIdField.setPrefWidth(300);
        userIdField.setFont(Font.font(16));

        // 学生姓名
        Label userNameLabel = new Label("学生姓名 *");
        userNameLabel.setFont(Font.font(16));
        TextField userNameField = new TextField();
        userNameField.setPrefWidth(300);
        userNameField.setFont(Font.font(16));

        // 课程名称
        Label courseNameLabel = new Label("课程名称 *");
        courseNameLabel.setFont(Font.font(16));
        TextField courseNameField = new TextField();
        courseNameField.setPrefWidth(300);
        courseNameField.setFont(Font.font(16));

        // 成绩（0-100）
        Label scoreValueLabel = new Label("成绩（0-100） *");
        scoreValueLabel.setFont(Font.font(15));
        TextField scoreValueField = new TextField();
        scoreValueField.setPrefWidth(300);
        scoreValueField.setFont(Font.font(16));

        // 考试类型（下拉选择，预设值）
        Label examTypeLabel = new Label("考试类型");
        examTypeLabel.setFont(Font.font(16));
        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.setPrefWidth(300);
        examTypeBox.getItems().addAll("期中", "期末", "补考", "其他");
        examTypeBox.setValue("其他"); // 默认值

        Label teacherIdLabel = new Label("教师ID *");
        teacherIdLabel.setFont(Font.font(16));
        TextField teacherIdField = new TextField();
        teacherIdField.setPrefWidth(300);
        teacherIdField.setFont(Font.font(16));
        // 可选：自动填充当前登录教师ID（如果当前用户是教师）
        if (roleId == 2) { // 假设roleId=2是教师角色
            teacherIdField.setText(Util.getUser().getUserId());
            teacherIdField.setEditable(false); // 不可编辑
        }

        Label teacherNameLabel = new Label("教师名称 *");
        teacherNameLabel.setFont(Font.font(16));
        TextField teacherNameField = new TextField();
        teacherNameField.setPrefWidth(300);
        teacherNameField.setFont(Font.font(16));
        // 自动填充当前登录教师名称
        if (roleId == 2) {
            teacherNameField.setText(Util.getUser().getRealName());
            teacherNameField.setEditable(false); // 不可编辑
        }

        // 提交/取消按钮
        Button submitBtn = new Button("提交");
        submitBtn.setPrefWidth(120);
        submitBtn.setPrefHeight(40);
        submitBtn.setFont(Font.font(18));

        Button cancelBtn = new Button("取消");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setFont(Font.font(18));

        // 将控件添加到GridPane（调整布局索引，适配新增字段）
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
        gridPane.add(teacherIdLabel, 0, 5);    // 新增教师ID行
        gridPane.add(teacherIdField, 1, 5);
        gridPane.add(teacherNameLabel, 0, 6);  // 新增教师名称行
        gridPane.add(teacherNameField, 1, 6);
        // 按钮位置后移（从5→7）
        gridPane.add(submitBtn, 0, 7);
        gridPane.add(cancelBtn, 1, 7);

        // 创建场景并设置到窗口
        Scene scene = new Scene(gridPane);
        addScoreStage.setScene(scene);

        // 取消按钮事件：关闭窗口
        cancelBtn.setOnAction(e -> addScoreStage.close());

        // 提交按钮事件：表单验证 + 数据封装 + 添加到列表 + 关闭窗口
        submitBtn.setOnAction(e -> {
            // 非空校验（新增教师ID、教师名称的校验）
            if (userIdField.getText().trim().isEmpty() || userNameField.getText().trim().isEmpty()
                    || courseNameField.getText().trim().isEmpty() || scoreValueField.getText().trim().isEmpty()
                    || teacherIdField.getText().trim().isEmpty() || teacherNameField.getText().trim().isEmpty()) { // 新增校验
                Util.showAlert(Alert.AlertType.ERROR, "错误", "信息填写不完整！");
                return;
            }

            if (!Objects.equals(getNameByUserId(userIdField.getText()), userNameField.getText())) {
                Util.showAlert(Alert.AlertType.ERROR, "错误", "学号与姓名不符！");
                return;
            }

            // 成绩格式及范围校验
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

            // 封装Score对象
            Score newScore = new Score();
            // 成绩ID
            newScore.setUserId(userIdField.getText().trim());
            newScore.setUserName(userNameField.getText().trim());
            newScore.setCourseName(courseNameField.getText().trim());
            newScore.setScoreValue(BigDecimal.valueOf(scoreValue));
            newScore.setExamType(examTypeBox.getValue());
            // 录入老师ID/名称：新增赋值
            newScore.setCreateUser(teacherIdField.getText().trim());
            // 创建时间/更新时间：当前时间戳
            Timestamp now = new Timestamp(System.currentTimeMillis());
            newScore.setCreateTime(now);
            newScore.setUpdateTime(now);

            // 添加到成绩列表并刷新表格
            scoreList.add(newScore);
            scoreService.addScore(newScore);

            // 提示成功并关闭窗口
            Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩新增成功！");
            addScoreStage.close();
        });

        // 显示窗口
        addScoreStage.showAndWait();
    }

    private void handleDelete() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要删除的成绩行！");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要删除【" + selectedScore.getUserName() + "-" + selectedScore.getCourseName() + "】的成绩吗？");
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                scoreList.remove(selectedScore);
                scoreService.deleteScore(selectedScore.getScoreId());
                Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩删除成功！");
            }
        });
    }

    private void handleQuery() {
        FilteredList<Score> filteredData = new FilteredList<>(scoreList, p -> true);
        filterData(filteredData);
        scoreTable.setItems(filteredData);
        Util.showAlert(Alert.AlertType.INFORMATION, "查询结果", "共查询到 " + scoreTable.getItems().size() + " 条成绩数据！");
    }

    private void handleUpdate() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要修改的成绩行！");
            return;
        }

        // 创建修改窗口
        Stage updateScoreStage = new Stage();
        updateScoreStage.setTitle("修改成绩"); // 修正标题
        updateScoreStage.setWidth(500);
        updateScoreStage.setHeight(680);
        updateScoreStage.setResizable(false);
        updateScoreStage.initModality(Modality.APPLICATION_MODAL);
        updateScoreStage.initOwner(scoreTable.getScene().getWindow());

        // 创建表单容器
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new javafx.geometry.Insets(30, 30, 30, 30));

        // ========== 创建表单控件并预填充选中数据 ==========
        // 学生ID
        Label userIdLabel = new Label("学生ID *");
        userIdLabel.setFont(Font.font(16));
        TextField userIdField = new TextField();
        userIdField.setPrefWidth(300);
        userIdField.setFont(Font.font(16));
        userIdField.setText(selectedScore.getUserId()); // 预填充
        userIdField.setEditable(false); // 禁用修改

        // 学生姓名
        Label userNameLabel = new Label("学生姓名 *");
        userNameLabel.setFont(Font.font(16));
        TextField userNameField = new TextField();
        userNameField.setPrefWidth(300);
        userNameField.setFont(Font.font(16));
        userNameField.setText(selectedScore.getUserName()); // 预填充

        // 课程名称
        Label courseNameLabel = new Label("课程名称 *");
        courseNameLabel.setFont(Font.font(16));
        TextField courseNameField = new TextField();
        courseNameField.setPrefWidth(300);
        courseNameField.setFont(Font.font(16));
        courseNameField.setText(selectedScore.getCourseName()); // 预填充

        // 成绩
        Label scoreValueLabel = new Label("成绩（0-100） *");
        scoreValueLabel.setFont(Font.font(15));
        TextField scoreValueField = new TextField();
        scoreValueField.setPrefWidth(300);
        scoreValueField.setFont(Font.font(16));
        scoreValueField.setText(selectedScore.getScoreValue() != null ?
                selectedScore.getScoreValue().toString() : "0"); // 预填充

        // 考试类型
        Label examTypeLabel = new Label("考试类型");
        examTypeLabel.setFont(Font.font(16));
        ComboBox<String> examTypeBox = new ComboBox<>();
        examTypeBox.setPrefWidth(300);
        examTypeBox.getItems().addAll("期中", "期末", "补考", "其他");
        examTypeBox.setValue(selectedScore.getExamType() != null ?
                selectedScore.getExamType() : "其他"); // 预填充

        // 教师ID
        Label teacherIdLabel = new Label("教师ID *");
        teacherIdLabel.setFont(Font.font(16));
        TextField teacherIdField = new TextField();
        teacherIdField.setPrefWidth(300);
        teacherIdField.setFont(Font.font(16));
        teacherIdField.setText(selectedScore.getCreateUser()); // 预填充
        if (roleId == 2) {
            teacherIdField.setText(Util.getUser().getUserId());
            teacherIdField.setEditable(false);
        }

        // 教师名称
        Label teacherNameLabel = new Label("教师名称 *");
        teacherNameLabel.setFont(Font.font(16));
        TextField teacherNameField = new TextField();
        teacherNameField.setPrefWidth(300);
        teacherNameField.setFont(Font.font(16));
        // 从用户表获取教师姓名
        String teacherName = userService.getUserById(teacherIdField.getText()).getRealName();
        teacherNameField.setText(teacherName);
        if (roleId == 2) {
            teacherNameField.setText(Util.getUser().getRealName());
            teacherNameField.setEditable(false);
        }

        // 提交/取消按钮
        Button submitBtn = new Button("提交");
        submitBtn.setPrefWidth(120);
        submitBtn.setPrefHeight(40);
        submitBtn.setFont(Font.font(18));

        Button cancelBtn = new Button("取消");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setFont(Font.font(18));

        // ========== 布局控件 ==========
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

        Scene scene = new Scene(gridPane);
        updateScoreStage.setScene(scene);

        // 取消按钮
        cancelBtn.setOnAction(e -> updateScoreStage.close());

        // 提交按钮（修改逻辑）
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

            // 姓名校验
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

            // ========== 更新选中的Score对象 ==========
            selectedScore.setUserName(userNameField.getText().trim());
            selectedScore.setCourseName(courseNameField.getText().trim());
            selectedScore.setScoreValue(BigDecimal.valueOf(scoreValue));
            selectedScore.setExamType(examTypeBox.getValue());
            selectedScore.setCreateUser(teacherIdField.getText().trim());
            selectedScore.setUpdateTime(new Timestamp(System.currentTimeMillis())); // 仅更新修改时间

            scoreService.updateScore(selectedScore);
            // 刷新表格（自动更新显示）
            scoreTable.refresh();

            // 提示+关闭窗口
            Util.showAlert(Alert.AlertType.INFORMATION, "成功", "成绩修改成功！");
            updateScoreStage.close();
        });

        // 显示窗口
        updateScoreStage.showAndWait();
    }

    private void handleView() {
        Score selectedScore = scoreTable.getSelectionModel().getSelectedItem();
        if (selectedScore == null) {
            Util.showAlert(Alert.AlertType.WARNING, "提示", "请先选中要查看的成绩行！");
            return;
        }

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

    public void setScoreList(ObservableList<Score> scoreList) {
        this.scoreList = scoreList == null ? FXCollections.observableArrayList() : scoreList;
        scoreTable.setItems(this.scoreList);
        initSearchFunction();
    }

    private String getNameByUserId(String userId) {
        return userService.getUserById(userId).getRealName();
    }
}