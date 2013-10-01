package com.yammer.breakerbox.service.core;

import java.net.URI;

public class SyncServiceHostState {
    private final URI uri;
    private final SyncStatus syncStatus;

    private SyncServiceHostState(URI uri, SyncStatus syncStatus) {
        this.uri = uri;
        this.syncStatus = syncStatus;
    }

    public URI getUri() {
        return uri;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public static SyncServiceHostState createSynchronized(URI uri) {
        return new SyncServiceHostState(uri, SyncStatus.SYNCHRONIZED);
    }

    public static SyncServiceHostState createUnsynchronized(URI uri) {
        return new SyncServiceHostState(uri, SyncStatus.UNSYNCHRONIZED);
    }

    public static SyncServiceHostState createUnknown(URI uri) {
        return new SyncServiceHostState(uri, SyncStatus.UNKNOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncServiceHostState syncServiceHostState = (SyncServiceHostState) o;

        if (syncStatus != syncServiceHostState.syncStatus) return false;
        if (!uri.equals(syncServiceHostState.uri)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + syncStatus.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SyncServiceHostState{" +
                "uri=" + uri +
                ", syncStatus=" + syncStatus +
                '}';
    }
}
