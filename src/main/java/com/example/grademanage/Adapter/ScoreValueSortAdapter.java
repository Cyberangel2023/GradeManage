package com.example.grademanage.Adapter;

import com.example.grademanage.Entity.Score;
import java.util.Comparator;

public class ScoreValueSortAdapter implements SortAdapter {
    @Override
    public Comparator<Score> getComparator(boolean asc) {
        // 成绩排序 + scoreId兜底
        Comparator<Score> baseComparator = Comparator.comparing(
                Score::getScoreValue,
                (bd1, bd2) -> {
                    if (bd1 == null && bd2 == null) return 0;
                    if (bd1 == null) return 1;
                    if (bd2 == null) return -1;
                    return bd1.compareTo(bd2);
                }
        ).thenComparing(Score::getScoreId, Comparator.nullsLast(Integer::compareTo));
        return asc ? baseComparator : baseComparator.reversed();
    }
}