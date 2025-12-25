package com.example.grademanage.Util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段信息注解（标记主键、自增、列名等）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
    /**
     * 是否为自增字段（仅主键生效）
     */
    boolean isAutoIncrement() default false;
}
