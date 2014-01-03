package com.yammer.breakerbox.service.comparable;

import com.yammer.breakerbox.azure.model.DependencyEntity;

import javax.annotation.Nullable;
import java.util.Comparator;

public class SortRowFirst implements Comparator<DependencyEntity> {

    @Override
    public int compare(@Nullable DependencyEntity left, @Nullable DependencyEntity right) {
        if (left == null || left.getRowKey() == null || "null".equals(left.getRowKey())) return 1;
        if (right == null || right.getRowKey() == null || "null".equals(right.getRowKey())) return -1;

        return Long.valueOf(right.getRowKey()).compareTo(Long.valueOf(left.getRowKey()));
    }
}
