package com.yammer.breakerbox.service.comparable;

import com.yammer.breakerbox.store.model.DependencyModel;

import java.util.Comparator;

public class SortRowFirst implements Comparator<DependencyModel> {

    @Override
    public int compare(DependencyModel left, DependencyModel right) {
        return left.getDateTime().compareTo(right.getDateTime());
    }
}
