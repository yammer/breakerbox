package com.yammer.breakerbox.service.core;

import java.net.URI;

public class SyncState {
    private final URI uri;
    private final SyncStatus syncStatus;

    private SyncState(URI uri, SyncStatus syncStatus) {
        this.uri = uri;
        this.syncStatus = syncStatus;
    }

    public URI getUri() {
        return uri;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public static SyncState createSynchronized(URI uri) {
        return new SyncState(uri, SyncStatus.SYNCHRONIZED);
    }

    public static SyncState createUnsynchronized(URI uri) {
        return new SyncState(uri, SyncStatus.UNSYNCHRONIZED);
    }

    public static SyncState createUnknown(URI uri) {
        return new SyncState(uri, SyncStatus.UNKNOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncState syncState = (SyncState) o;

        if (syncStatus != syncState.syncStatus) return false;
        if (!uri.equals(syncState.uri)) return false;

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
        return "SyncState{" +
                "uri=" + uri +
                ", syncStatus=" + syncStatus +
                '}';
    }
}
