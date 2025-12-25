package com.example.grademanage.Entity;

import com.example.grademanage.Util.TableId;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User {
    @TableId
    private String userId; // 用户ID（学号/工号）
    private String realName; // 真实姓名
    private String password; // 加密密码
    private String gender; // 性别
    private String phone; // 手机号
    private String email; // 邮箱
    private Integer roleId; // 角色ID
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间

    public User(String userId, String password, String realName,
                String gender, String phone, String email, Integer roleId,
                Date createTime, Date updateTime) {
        this.userId = userId;
        this.realName = realName;
        this.password = password;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.roleId = roleId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public User() {}
}
