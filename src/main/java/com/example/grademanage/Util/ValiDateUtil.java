package com.example.grademanage.Util;

import com.example.grademanage.Entity.User;

// 数据校验
public class ValiDateUtil {
    // 校验ID
    public static boolean valiDateID(Integer ID) {
        return ID > 0;
    }

    // 校验名字
    public static boolean valiDateName(String name){
        if (name == null || name.length() > 20) {
            System.out.println("错误：名字长度必须≤20个字符");
            return false;
        }
        String nameRegex = "^[a-zA-Z_][a-zA-Z0-9_]*$";
        if (!name.matches(nameRegex)) {
            System.out.println("错误：名字只能由字母、数字、下划线组成，且首字符不能是数字");
            return false;
        }
        return true;
    }

    // 校验用户名
    public static boolean validateUserId(String userId) {
        // 1. 基础非空校验
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("错误：用户ID不能为空");
            return false;
        }
        // 2. 纯数字校验（支持正整数，不含负号/小数点）
        String numberRegex = "^\\d+$";
        if (!userId.matches(numberRegex)) {
            System.out.println("错误：用户ID必须由纯数字组成");
            return false;
        }
        return true;
    }

    // 校验密码
    public static String validatePassword(String password) {
        // 基础非空+长度校验
        if (password == null || password.trim().isEmpty()) {
            return "错误：密码不能为空！";
        }
        if (password.length() < 8 || password.length() > 20) {
            return "错误：密码长度必须在8-20位之间！";
        }
        // 复杂度校验：至少包含1个字母 + 1个数字（可根据需求调整）
        boolean hasLetter = password.matches(".*[a-zA-Z]+.*"); // 包含字母
        boolean hasNumber = password.matches(".*\\d+.*");      // 包含数字
        if (!hasLetter || !hasNumber) {
            return"错误：密码必须同时包含字母和数字！";
        }
        return "";
    }

    // 校验用户对象参数
    public static boolean validateUserParam(User user, boolean isAdd) {
        if (user == null) {
            System.out.println("错误：用户对象不能为空");
            return false;
        }
        // 新增用户必须校验ID和密码
        if (isAdd) {
            if (!validateUserId(user.getUserId())) {
                return false;
            }
            if (!validatePassword(user.getPassword()).isEmpty()) {
                return false;
            }
        }
        // 校验真实姓名
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            System.out.println("错误：真实姓名不能为空");
            return false;
        }
        return true;
    }
}
