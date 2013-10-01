package com.yammer.breakerbox.service.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncPropertyKeyState {
    private final String propertyKey;
    private final SyncStatus syncStatus;

    public SyncPropertyKeyState(String propertyKey, SyncStatus syncStatus) {
        this.propertyKey = propertyKey;
        this.syncStatus = syncStatus;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    @JsonIgnore
    public boolean isSynchronized() {
        return syncStatus == SyncStatus.SYNCHRONIZED;
    }

    @JsonIgnore
    public boolean isUnsynchronized() {
        return syncStatus == SyncStatus.UNSYNCHRONIZED;
    }

    @JsonIgnore
    public boolean isUnknown() {
        return syncStatus == SyncStatus.UNKNOWN;
    }

    public static SyncPropertyKeyState createSynchronized(String propertyKey) {
        return new SyncPropertyKeyState(propertyKey, SyncStatus.SYNCHRONIZED);
    }

    public static SyncPropertyKeyState createUnsynchronized(String propertyKey) {
        return new SyncPropertyKeyState(propertyKey, SyncStatus.UNSYNCHRONIZED);
    }

    public static SyncPropertyKeyState createUnknown(String propertyKey) {
        return new SyncPropertyKeyState(propertyKey, SyncStatus.UNKNOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncPropertyKeyState that = (SyncPropertyKeyState) o;

        if (!propertyKey.equals(that.propertyKey)) return false;
        if (syncStatus != that.syncStatus) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyKey.hashCode();
        result = 31 * result + syncStatus.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SyncPropertyKeyState{" +
                "propertyKey='" + propertyKey + '\'' +
                ", syncStatus=" + syncStatus +
                '}';
    }
}