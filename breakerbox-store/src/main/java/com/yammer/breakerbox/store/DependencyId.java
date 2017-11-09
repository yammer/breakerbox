package com.yammer.breakerbox.store;

import com.yammer.tenacity.core.properties.TenacityPropertyKey;

import java.util.Objects;

public class DependencyId implements TenacityPropertyKey {
    private final String id;

    private DependencyId(String id) {
        this.id = id.toUpperCase();
    }

    public String getId() {
        return id;
    }

    public static DependencyId from(String id) {
        return new DependencyId(id);
    }

    @Override
    public String name() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DependencyId other = (DependencyId) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return id;
    }
}