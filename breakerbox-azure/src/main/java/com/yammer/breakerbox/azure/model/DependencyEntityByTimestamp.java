package com.yammer.breakerbox.azure.model;

import com.google.common.primitives.Longs;

import java.util.Comparator;

public class DependencyEntityByTimestamp implements Comparator<DependencyEntity> {
    @Override
    public int compare(DependencyEntity o1, DependencyEntity o2) {
        return Longs.compare(o1.getConfigurationTimestamp(), o2.getConfigurationTimestamp());
    }
}