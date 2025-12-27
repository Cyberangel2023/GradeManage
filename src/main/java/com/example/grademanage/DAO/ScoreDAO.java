package com.example.grademanage.DAO;

import com.example.grademanage.Entity.Score;
import com.example.grademanage.Util.ORMUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩DAO层（完整CRUD + 多条件查询 + 规范资源管理）
 */
public class ScoreDAO {

    /**
     * 保存成绩信息（新增）
     */
    public void save(Score score) {
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createSaveSQL(score))) {
            pstmt.executeUpdate();
            System.out.println("成绩数据保存成功，执行SQL：" + ORMUtil.createSaveSQL(score));
        } catch (Exception e) {
            e.printStackTrace();
//            String userId = score.getUserId() == null ? "未设置" : score.getUserId();
//            String course = score.getCourseName() == null ? "未知课程" : score.getCourseName();
//            throw new RuntimeException("保存成绩失败，用户ID：" + userId + "，课程：" + course, e);
        }
    }

    /**
     * 根据成绩ID删除成绩
     */
    public void delete(Integer scoreId) {
        if (scoreId == null) {
            throw new IllegalArgumentException("删除成绩失败：成绩ID不能为null");
        }
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createDeleteSQL(Score.class, scoreId))) {

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("删除成绩失败：未找到ID为" + scoreId + "的成绩记录");
            } else {
                System.out.println("成绩数据删除成功，执行SQL：" + ORMUtil.createDeleteSQL(Score.class, scoreId));
            }
        } catch (Exception e) {
            throw new RuntimeException("删除成绩失败，成绩ID：" + scoreId, e);
        }
    }

    /**
     * 更新成绩信息
     */
    public void update(Score score) {
        if (score.getScoreId() == null) {
            throw new IllegalArgumentException("更新成绩失败：成绩ID不能为null");
        }
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ORMUtil.createUpdateSQL(score))) {

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("更新成绩失败：未找到ID为" + score.getScoreId() + "的成绩记录");
            } else {
                System.out.println("成绩数据修改成功，执行SQL：" + ORMUtil.createUpdateSQL(score));
            }
        } catch (Exception e) {
            throw new RuntimeException("更新成绩失败，成绩ID：" + score.getScoreId(), e);
        }
    }

    /**
     * 根据成绩ID查询单个成绩
     */
    public Score findById(Integer scoreId) {
        if (scoreId == null) {
            throw new IllegalArgumentException("查询成绩失败：成绩ID不能为null");
        }
        String sql = ORMUtil.createSelectSQL(Score.class, scoreId);
        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return resultSetToScore(rs);
            } else {
                System.out.println("未找到ID为" + scoreId + "的成绩记录");
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("根据ID查询成绩失败，成绩ID：" + scoreId, e);
        }
    }

    /**
     * 根据用户ID查询该用户所有成绩
     */
    public List<Score> findByUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("查询成绩失败：用户ID不能为null");
        }
        // 复用ORMUtil的表名配置，避免硬编码
        String tableName = (String) ORMUtil.tableMetaCache.get("Score").get("tableName");
        String sql = String.format("SELECT * FROM %s WHERE user_id = ?", tableName);
        List<Score> scoreList = new ArrayList<>();

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scoreList.add(resultSetToScore(rs));
                }
            }
            System.out.println("用户ID[" + userId + "]共查询到" + scoreList.size() + "条成绩记录");
            return scoreList;
        } catch (Exception e) {
            throw new RuntimeException("根据用户ID查询成绩失败，用户ID：" + userId, e);
        }
    }

    /**
     * 根据课程名模糊查询成绩（支持多结果）
     */
    public List<Score> findByCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("查询成绩失败：课程名不能为null/空");
        }
        String tableName = (String) ORMUtil.tableMetaCache.get("Score").get("tableName");
        String sql = String.format("SELECT * FROM %s WHERE course_name LIKE ?", tableName);
        List<Score> scoreList = new ArrayList<>();

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + courseName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scoreList.add(resultSetToScore(rs));
                }
            }
            System.out.println("课程名[" + courseName + "]共查询到" + scoreList.size() + "条成绩记录");
            return scoreList;
        } catch (Exception e) {
            throw new RuntimeException("根据课程名查询成绩失败，课程名：" + courseName, e);
        }
    }

    /**
     * 查询全部成绩记录
     */
    public List<Score> findAll() {
        String sql = ORMUtil.createSelectAllSQL(Score.class);
        List<Score> scoreList = new ArrayList<>();

        try (Connection conn = ORMUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                scoreList.add(resultSetToScore(rs));
            }
            System.out.println("查询到全部成绩共" + scoreList.size() + "条记录");
            return scoreList;
        } catch (Exception e) {
            throw new RuntimeException("查询全部成绩失败", e);
        }
    }

    /**
     * 工具方法：将ResultSet解析为Score对象（复用逻辑）
     */
    private Score resultSetToScore(ResultSet rs) throws SQLException {
        Score score = new Score();
        score.setScoreId(rs.getInt("score_id"));
        score.setUserId(rs.getString("user_id"));
        score.setUserName(rs.getString("user_name"));
        score.setCourseName(rs.getString("course_name"));
        score.setScoreValue(rs.getBigDecimal("score_value"));
        score.setExamType(rs.getString("exam_type"));
        score.setCreateUser(rs.getString("create_user"));
        score.setCreateTime(rs.getTimestamp("create_time"));
        score.setUpdateTime(rs.getTimestamp("update_time"));
        return score;
    }
}