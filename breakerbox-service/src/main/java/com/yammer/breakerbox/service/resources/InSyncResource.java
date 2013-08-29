package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.core.ServiceId;
import com.yammer.breakerbox.service.core.SyncComparator;
import com.yammer.breakerbox.service.core.SyncState;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/sync/{service}/{dependency}")
public class InSyncResource {
    private final SyncComparator syncComparator;

    public InSyncResource(SyncComparator syncComparator) {
        this.syncComparator = checkNotNull(syncComparator);
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<SyncState> insync(@PathParam("service") String serviceName,
                                      @PathParam("dependency") String dependencyName) {
        return syncComparator.inSync(ServiceId.from(serviceName), DependencyId.from(dependencyName));
    }
}