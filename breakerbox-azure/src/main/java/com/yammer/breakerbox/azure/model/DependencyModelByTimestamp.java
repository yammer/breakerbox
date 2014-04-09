package com.yammer.breakerbox.azure.model;

import com.yammer.breakerbox.store.model.DependencyModel;

import java.util.Comparator;

public class DependencyModelByTimestamp implements Comparator<DependencyModel> {
    @Override
    public int compare(DependencyModel o1, DependencyModel o2) {
        return o1.getDateTime().compareTo(o2.getDateTime());
    }
}