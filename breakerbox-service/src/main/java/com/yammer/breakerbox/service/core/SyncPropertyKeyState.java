package com.yammer.breakerbox.service.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(propertyKey, syncStatus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SyncPropertyKeyState other = (SyncPropertyKeyState) obj;
        return Objects.equals(this.propertyKey, other.propertyKey)
                && Objects.equals(this.syncStatus, other.syncStatus);
    }

    @Override
    public String toString() {
        return "SyncPropertyKeyState{" +
                "propertyKey='" + propertyKey + '\'' +
                ", syncStatus=" + syncStatus +
                '}';
    }
}