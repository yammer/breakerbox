package com.yammer.breakerbox.service.comparable;

import com.yammer.breakerbox.store.model.DependencyModel;

import java.io.Serializable;
import java.util.Comparator;

public class SortRowFirst implements Comparator<DependencyModel>, Serializable {
    private static final long serialVersionUID = 1299382937789000L;

    @Override
    public int compare(DependencyModel left, DependencyModel right) {
        return left.getDateTime().compareTo(right.getDateTime());
    }
}
