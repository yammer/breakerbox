package com.yammer.breakerbox.service.core;

public class EnvironmentId {
    private final String id;

    private EnvironmentId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EnvironmentId from(String id) {
        return new EnvironmentId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentId that = (EnvironmentId) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
