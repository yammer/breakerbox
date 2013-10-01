package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.core.*;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/sync/{service}")
public class InSyncResource {
    private final SyncComparator syncComparator;
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;

    public InSyncResource(SyncComparator syncComparator,
                          TenacityPropertyKeysStore tenacityPropertyKeysStore) {
        this.syncComparator = checkNotNull(syncComparator);
        this.tenacityPropertyKeysStore = checkNotNull(tenacityPropertyKeysStore);
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON) @Path("/{dependency}")
    public Iterable<SyncServiceHostState> inSync(@PathParam("service") String serviceName,
                                                 @PathParam("dependency") String dependencyName) {
        return syncComparator.inSync(ServiceId.from(serviceName), DependencyId.from(dependencyName));
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Iterable<SyncPropertyKeyState> allInSync(@PathParam("service") String serviceName) {
        final ServiceId serviceId = ServiceId.from(serviceName);
        return syncComparator.allInSync(serviceId,
                tenacityPropertyKeysStore.tenacityPropertyKeysFor(Instances.propertyKeyUris(serviceId)));
    }
}