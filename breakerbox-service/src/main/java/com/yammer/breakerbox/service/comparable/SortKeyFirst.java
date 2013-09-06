package com.yammer.breakerbox.service.comparable;

import com.google.common.collect.ComparisonChain;
import com.yammer.breakerbox.service.core.DependencyId;

import java.util.Comparator;

public class SortKeyFirst implements Comparator<String> {
    private final String sortFirst;

    public SortKeyFirst(DependencyId sortFirst) {
        this.sortFirst = sortFirst.getId();
    }

    @Override
    public int compare(String o1, String o2) {
        return ComparisonChain
                .start()
                .compareTrueFirst(o1.equals(sortFirst), o2.equals(sortFirst))
                .result();
    }
}
