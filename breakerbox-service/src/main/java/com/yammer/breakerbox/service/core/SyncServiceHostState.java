package com.yammer.breakerbox.service.core;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class SyncServiceHostState {
    private final String id;
    private final SyncStatus syncStatus;

    public SyncServiceHostState(String id, SyncStatus syncStatus) {
        this.id = id;
        this.syncStatus = syncStatus;
    }

    public String getId() {
        return id;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public static SyncServiceHostState createSynchronized(String id) {
        return new SyncServiceHostState(id, SyncStatus.SYNCHRONIZED);
    }

    public static SyncServiceHostState createUnsynchronized(String id) {
        return new SyncServiceHostState(id, SyncStatus.UNSYNCHRONIZED);
    }

    public static SyncServiceHostState createUnknown(String id) {
        return new SyncServiceHostState(id, SyncStatus.UNKNOWN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, syncStatus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SyncServiceHostState other = (SyncServiceHostState) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.syncStatus, other.syncStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("syncStatus", syncStatus)
                .toString();
    }
}
