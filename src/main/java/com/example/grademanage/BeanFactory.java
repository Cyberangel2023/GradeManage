package com.example.grademanage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactory {
    private static BeanFactory instance;
    private final Map<String, Object> beans = new HashMap<>();

    private BeanFactory() {
        try {
            Gson gson = new Gson();
            Type rootType = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            Map<String, List<Map<String, String>>> rootMap = gson.fromJson(new FileReader("beans.json"), rootType);
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

    public static BeanFactory getInstance() {
        if (instance == null) {
            instance = new BeanFactory();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) beans.get(name);
    }

    // 数据校验方法
    public static boolean valiDateID(Integer ID) {
        if (ID <= 0) {
            System.out.println("错误：ID必须是大于0的整数");
            return false;
        }
        return true;
    }

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
}