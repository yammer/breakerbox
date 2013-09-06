package com.yammer.breakerbox.service.comparable;

import com.yammer.azure.core.TableType;

import javax.annotation.Nullable;
import java.util.Comparator;


public class DescendingRowOrder<T extends TableType> implements Comparator<T> {

    @Override
    public int compare(@Nullable T left, @Nullable T right) {
        //force nulls to the end
        if (left == null || left.getRowKey() == null || "null".equals(left.getRowKey())) return 1;
        if (right == null || right.getRowKey() == null || "null".equals(right.getRowKey())) return -1;

        return Long.valueOf(right.getRowKey()).compareTo(Long.valueOf(left.getRowKey()));
    }
}
