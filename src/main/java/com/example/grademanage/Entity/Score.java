package com.example.grademanage.Entity;

import com.example.grademanage.Util.TableId;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class Score {
    @TableId(isAutoIncrement = true)
    private Integer scoreId; // 成绩ID
    private String userId; // 学生ID
    private String userName; // 学生姓名
    private String courseName; // 课程名称
    private BigDecimal scoreValue; // 成绩
    private String examType; // 考试类型
    private String createUser; // 录入老师ID
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间

    public Score(Integer scoreId, String userId, String userName, String courseName,
                 BigDecimal scoreValue, String examType, String createUser,
                 Date createTime, Date updateTime) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.userName = userName;
        this.courseName = courseName;
        this.scoreValue = scoreValue;
        this.examType = examType;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Score() {}
}
