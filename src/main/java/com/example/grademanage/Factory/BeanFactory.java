package com.example.grademanage.Factory;

/**
 * Bean工厂接口
 * 定义Bean的获取、数据校验等核心功能规范
 */
public interface BeanFactory {

    /**
     * 根据Bean名称获取对应的Bean实例
     * @param name Bean的名称（对应配置文件中的bean属性）
     * @param <T>  Bean的类型泛型
     * @return 对应的Bean实例，如果不存在则返回null
     */
    <T> T getBean(String name);}