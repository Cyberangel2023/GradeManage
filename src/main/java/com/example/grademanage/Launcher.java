package com.example.grademanage;

public class Launcher {
    public static void main(String[] args) {
        javafx.application.Application.launch(Application.class, args);
    }
}

// 测试
//        User user = new User();
//        user.setUserId("2025001");
//        user.setRealName("张三");
//        user.setPassword("123456");
//        user.setGender("男");
//        user.setRoleId(1);
//        userService.addUser(user); // 新增用户
//        user.setUserId("2025002");
//        user.setRealName("李四");
//        user.setRoleId(2);
//        userService.addUser(user); // 新增用户
//
//        User queryUser = userService.getUserById("2025001"); // 查询用户
//        System.out.println("查询到用户：" + queryUser.getRealName());
//        queryUser = userService.getUserById("2025002"); // 查询用户
//        System.out.println("查询到用户：" + queryUser.getRealName());
//
//        // 2. 成绩服务使用示例
//        Score score = new Score();
//        score.setScoreId(1);
//        score.setUserId("2025001");
//        score.setCourseName("高等数学");
//        score.setScoreValue(new BigDecimal("92.5"));
//        score.setExamType("期末");
//        score.setCreateUser("2025002");
//        scoreService.addScore(score); // 新增成绩
//
//        List<Score> userScores = scoreService.getScoresByUserId("2025001"); // 查询用户所有成绩
//        userScores.forEach(s -> System.out.println(s.getCourseName() + "：" + s.getScoreValue()));