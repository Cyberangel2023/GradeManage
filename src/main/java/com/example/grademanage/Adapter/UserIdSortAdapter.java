package com.example.grademanage.Adapter;

import com.example.grademanage.Entity.Score;
import java.util.Comparator;

public class UserIdSortAdapter implements SortAdapter {
    @Override
    public Comparator<Score> getComparator(boolean asc) {
        // 学号排序 + scoreId兜底
        Comparator<Score> baseComparator = Comparator.comparing(Score::getUserId, Comparator.nullsLast(String::compareTo))
                .thenComparing(Score::getScoreId, Comparator.nullsLast(Integer::compareTo));
        return asc ? baseComparator : baseComparator.reversed();
    }
}