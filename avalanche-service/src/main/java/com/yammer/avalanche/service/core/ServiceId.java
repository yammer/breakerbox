package com.yammer.avalanche.service.core;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceId serviceId = (ServiceId) o;

        if (!id.equals(serviceId.id)) return false;

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
