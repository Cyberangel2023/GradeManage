package com.example.grademanage.Factory;

import com.example.grademanage.Factory.Impl.BeanFactoryImpl;
import com.example.grademanage.Factory.Impl.PermissionFactoryImpl;

/**
 * 工厂生产者（统一工厂入口）
 * 扩展原有DAO工厂，新增权限工厂获取能力
 */
public class FactoryProducer {
    // 工厂类型枚举
    public enum FactoryType {
        PERMISSION, BEAN
    }

    /**
     * 获取工厂
     */
    public static Object getFactory(FactoryType type) {
        return switch (type) {
            case PERMISSION -> PermissionFactoryImpl.getInstance();
            case BEAN -> BeanFactoryImpl.getInstance();
        };
    }
}