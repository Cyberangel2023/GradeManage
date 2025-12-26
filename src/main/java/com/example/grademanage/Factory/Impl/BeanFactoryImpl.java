package com.example.grademanage.Factory.Impl;

import com.example.grademanage.Factory.BeanFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactoryImpl implements BeanFactory {
    private static BeanFactoryImpl instance;
    private final Map<String, Object> beans = new HashMap<>();

    public static BeanFactoryImpl getInstance() {
        if (instance == null) {
            instance = new BeanFactoryImpl();
        }
        return instance;
    }

    private BeanFactoryImpl() {
        try {
            Gson gson = new Gson();
            Type rootType = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/beans.json");
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Map<String, List<Map<String, String>>> rootMap = gson.fromJson(reader, rootType);
            List<Map<String, String>> beanConfigs = rootMap.get("beans");

            for (Map<String, String> config : beanConfigs) {
                String className = config.get("classname");
                String beanName = config.get("bean");
                if (className == null || beanName == null) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object beanInstance = constructor.newInstance();
                beans.put(beanName, beanInstance);
            }

            for (Map<String, String> config : beanConfigs) {
                String beanName = config.get("bean");
                Object beanInstance = beans.get(beanName);
                if (beanInstance == null) {
                    continue;
                }

                // 遍历配置中的所有属性，通过setter方法注入
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    String propertyName = entry.getKey();
                    String propertyValue = entry.getValue();

                    // 跳过classname和bean这两个特殊属性
                    if ("classname".equals(propertyName) || "bean".equals(propertyName)) {
                        continue;
                    }

                    // 依赖注入
                    setProperty(beanInstance, propertyName, beans.getOrDefault(propertyValue, propertyValue));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过反射调用setter方法为属性赋值
     * @param obj 目标对象
     * @param propertyName 属性名
     * @param value 属性值
     */
    private void setProperty(Object obj, String propertyName, Object value) throws Exception {
        // 生成setter方法名
        String setterMethodName = "set" +
                propertyName.substring(0, 1).toUpperCase() +
                propertyName.substring(1);

        // 获取属性类型
        Class<?> propertyType = getPropertyType(obj.getClass(), propertyName);
        if (propertyType == null) {
            return;
        }

        // 找到对应的setter方法并调用
        Method setterMethod = obj.getClass().getMethod(setterMethodName, propertyType);
        setterMethod.invoke(obj, value);
    }

    /**
     * 获取对象的属性类型
     */
    private Class<?> getPropertyType(Class<?> clazz, String propertyName) {
        try {
            return clazz.getDeclaredField(propertyName).getType();
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getPropertyType(clazz.getSuperclass(), propertyName);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) beans.get(name);
    }
}