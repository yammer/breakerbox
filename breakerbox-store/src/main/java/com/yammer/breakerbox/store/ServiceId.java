package com.yammer.breakerbox.store;

import java.util.Objects;

public class ServiceId {
    private final String id;

    private ServiceId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ServiceId from(String id) {
        return new ServiceId(id);
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
        final ServiceId other = (ServiceId) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return id;
    }
}
