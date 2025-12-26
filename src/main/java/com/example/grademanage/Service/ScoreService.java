package com.example.grademanage.Service;

import com.example.grademanage.Factory.Impl.BeanFactoryImpl;
import com.example.grademanage.DAO.ScoreDAO;
import com.example.grademanage.Entity.Score;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩业务层（封装业务逻辑 + 分数校验 + 关联用户校验）
 * 修正：userId/createUser改为String类型，适配数据库VARCHAR(30)
 */
public class ScoreService {
    // 依赖注入DAO
    @Getter
    @Setter
    private ScoreDAO dao;
    // 依赖用户服务（校验用户是否存在）
    private static final UserService userService;

    static {
        BeanFactoryImpl beanFactoryImpl = BeanFactoryImpl.getInstance();
        userService = beanFactoryImpl.getBean("userService");
    }
    /**
     * 新增成绩
     * @param score 成绩对象（必填：userId/courseName/scoreValue/examType）
     */
    public void addScore(Score score) {
        // 1. 业务参数校验
        validateScoreParam(score, true);

        // 2. 业务逻辑：校验用户是否存在（userId改为String）
        try {
            System.out.println(score.getUserId());
            userService.getUserById(score.getUserId());
        } catch (RuntimeException e) {
            throw new RuntimeException("新增成绩失败：关联的用户ID[" + score.getUserId() + "]不存在", e);
        }

        // 3. 业务逻辑：校验分数范围（0-100）
        if (score.getScoreValue().compareTo(BigDecimal.ZERO) < 0 || score.getScoreValue().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("新增成绩失败：分数[" + score.getScoreValue() + "]超出0-100范围");
        }

        // 4. 调用DAO层保存
        dao.save(score);
        System.out.println("成绩新增成功：用户ID[" + score.getUserId() + "]，课程[" + score.getCourseName() + "]");
    }

    /**
     * 删除成绩
     * @param scoreId 成绩ID（仍为Integer，根据数据库结构）
     */
    public void deleteScore(Integer scoreId) {
        if (scoreId == null || scoreId <= 0) {
            throw new IllegalArgumentException("删除成绩失败：成绩ID必须为正整数");
        }

        // 校验成绩是否存在
        Score existScore = dao.findById(scoreId);
        if (existScore == null) {
            throw new RuntimeException("删除成绩失败：ID为" + scoreId + "的成绩不存在");
        }

        dao.delete(scoreId);
        System.out.println("成绩删除成功：ID[" + scoreId + "]，课程[" + existScore.getCourseName() + "]");
    }

    /**
     * 更新成绩
     * @param score 成绩对象（必填：scoreId）
     */
    public void updateScore(Score score) {
        // 参数校验
        validateScoreParam(score, false);

        // 校验成绩是否存在
        Score existScore = dao.findById(score.getScoreId());
        if (existScore == null) {
            throw new RuntimeException("更新成绩失败：ID为" + score.getScoreId() + "的成绩不存在");
        }

        // 校验分数范围（若更新了分数）
        if (score.getScoreValue() != null) {
            if (score.getScoreValue().compareTo(BigDecimal.ZERO) < 0 || score.getScoreValue().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("更新成绩失败：分数[" + score.getScoreValue() + "]超出0-100范围");
            }
        }

        dao.update(score);
        System.out.println("成绩更新成功：ID[" + score.getScoreId() + "]，课程[" + score.getCourseName() + "]");
    }

    /**
     * 根据ID查询成绩
     * @param scoreId 成绩ID
     * @return 成绩对象
     */
    public Score getScoreById(Integer scoreId) {
        if (scoreId == null || scoreId <= 0) {
            throw new IllegalArgumentException("查询成绩失败：成绩ID必须为正整数");
        }
        Score score = dao.findById(scoreId);
        if (score == null) {
            throw new RuntimeException("查询成绩失败：ID为" + scoreId + "的成绩不存在");
        }
        return score;
    }

    /**
     * 根据用户ID查询所有成绩
     * @param userId 用户ID（String类型）
     * @return 成绩列表
     */
    public List<Score> getScoresByUserId(String userId) {
        // 校验改为String非空
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("查询成绩失败：用户ID不能为空");
        }
        // 校验用户是否存在
        try {
            userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("查询成绩失败：用户ID[" + userId + "]不存在", e);
        }

        List<Score> scoreList = dao.findByUserId(userId);
        if (scoreList.isEmpty()) {
            System.out.println("查询提示：用户ID[" + userId + "]暂无成绩数据");
        }
        return scoreList;
    }

    /**
     * 根据课程名模糊查询成绩
     * @param courseName 课程名
     * @return 成绩列表
     */
    public List<Score> getScoresByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("查询成绩失败：课程名不能为空");
        }
        List<Score> scoreList = dao.findByCourseName(courseName);
        if (scoreList.isEmpty()) {
            System.out.println("查询提示：未找到课程名包含[" + courseName + "]的成绩数据");
        }
        return scoreList;
    }

    /**
     * 查询所有成绩
     * @return 所有成绩列表
     */
    public List<Score> getAllScores() {
        List<Score> scoreList = dao.findAll();
        if (scoreList.isEmpty()) {
            System.out.println("查询提示：暂无成绩数据");
        }
        return scoreList;
    }

    /**
     * 私有工具方法：成绩参数校验
     * @param score 成绩对象
     * @param isAdd 是否为新增操作
     */
    private void validateScoreParam(Score score, boolean isAdd) {
        if (score == null) {
            throw new IllegalArgumentException("成绩对象不能为null");
        }
        if (isAdd) {
            // 新增必填项：userId改为String非空校验
            if (score.getUserId() == null || score.getUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("新增成绩失败：用户ID不能为空");
            }
            if (score.getCourseName() == null || score.getCourseName().trim().isEmpty()) {
                throw new IllegalArgumentException("新增成绩失败：课程名不能为空");
            }
            if (score.getScoreValue() == null) {
                throw new IllegalArgumentException("新增成绩失败：分数不能为空");
            }
            if (score.getExamType() == null || score.getExamType().trim().isEmpty()) {
                throw new IllegalArgumentException("新增成绩失败：考试类型不能为空");
            }
        } else {
            // 更新必填项：scoreId仍为Integer
            if (score.getScoreId() == null || score.getScoreId() <= 0) {
                throw new IllegalArgumentException("更新成绩失败：成绩ID必须为正整数");
            }
        }
    }
}