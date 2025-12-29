package com.example.grademanage.Adapter;

import com.example.grademanage.Entity.Score;
import java.util.Comparator;

/**
 * 成绩排序适配器接口
 * 定义不同维度排序的统一规范
 */
public interface SortAdapter {
    /**
     * 获取指定升降序的比较器
     * @param asc 是否升序
     * @return 排序比较器
     */
    Comparator<Score> getComparator(boolean asc);
}