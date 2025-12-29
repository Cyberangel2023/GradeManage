package com.example.grademanage.Adapter;

import com.example.grademanage.Entity.Score;
import java.util.Comparator;

public class CourseNameSortAdapter implements SortAdapter {
    @Override
    public Comparator<Score> getComparator(boolean asc) {
        // 课程排序 + scoreId兜底
        Comparator<Score> baseComparator = Comparator.comparing(Score::getCourseName, Comparator.nullsLast(String::compareTo))
                .thenComparing(Score::getScoreId, Comparator.nullsLast(Integer::compareTo));
        return asc ? baseComparator : baseComparator.reversed();
    }
}