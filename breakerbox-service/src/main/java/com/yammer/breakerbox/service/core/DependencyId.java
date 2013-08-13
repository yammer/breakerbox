package com.yammer.breakerbox.service.core;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;

public class DependencyId implements TenacityPropertyKey {
    private final String id;

    private DependencyId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static DependencyId from(String id) {
        return new DependencyId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependencyId that = (DependencyId) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}