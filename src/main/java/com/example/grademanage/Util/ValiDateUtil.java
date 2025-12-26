package com.example.grademanage.Util;

// 数据校验
public class ValiDateUtil {
    public boolean valiDateID(Integer ID) {
        if (ID <= 0) {
            System.out.println("错误：ID必须是大于0的整数");
            return false;
        }
        return true;
    }

    public boolean valiDateName(String name){
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
}
