package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Ordering;
import com.yammer.breakerbox.service.comparable.SortRowFirst;
import com.yammer.breakerbox.service.core.Instances;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.store.BreakerboxStore;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Set;

@Path("/clusters")
public class ClustersResource {
    private final Set<String> specifiedMetaClusters;
    private final BreakerboxStore breakerboxStore;
    private final TenacityPropertyKeysStore tenacityPropertyKeysStore;

    public ClustersResource(Set<String> specifiedMetaClusters,
                            BreakerboxStore breakerboxStore,
                            TenacityPropertyKeysStore tenacityPropertyKeysStore) {
        this.specifiedMetaClusters = specifiedMetaClusters;
        this.breakerboxStore = breakerboxStore;
        this.tenacityPropertyKeysStore = tenacityPropertyKeysStore;
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> clusters(@QueryParam("no-meta") @DefaultValue("false") boolean noMeta) {
        return noMeta ? Instances.noMetaClusters(specifiedMetaClusters) : Instances.clusters();
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    @Path("{serviceId}/propertykeys")
    public Collection<String> propertyKeys(@PathParam("serviceId") String id) {
        final ServiceId serviceId = ServiceId.from(id);
        return tenacityPropertyKeysStore.tenacityPropertyKeysFor(Instances.propertyKeyUris(serviceId));
    }

    @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    @Path("{serviceId}/configurations/{dependencyId}")
    public Collection<DependencyModel> configurations(@PathParam("serviceId") String serviceId,
                                                      @PathParam("dependencyId") String dependencyId) {
        return Ordering.from(new SortRowFirst())
                .reverse()
                .immutableSortedCopy(breakerboxStore.allDependenciesFor(DependencyId.from(dependencyId),
                        ServiceId.from(serviceId)));
    }
}