package com.yammer.breakerbox.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.net.HostAndPort;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Path("/")
public class DashboardResource {
    private final String defaultDashboard;
    private final HostAndPort breakerboxHostAndPort;
    private final Set<String> specifiedMetaClusters;
    private final Supplier<byte[]> indexSupplier;

    public DashboardResource(String defaultDashboard,
                             HostAndPort breakerboxHostAndPort,
                             Set<String> specifiedMetaClusters) {
        this.defaultDashboard = defaultDashboard;
        this.breakerboxHostAndPort = breakerboxHostAndPort;
        this.specifiedMetaClusters = specifiedMetaClusters;
        indexSupplier = Suppliers.memoize(
                () -> {
                    try {
                        return Resources.asByteSource(Resources.getResource("index.html"))
                                .read();
                    } catch (IOException err) {
                        throw new IllegalStateException(err);
                    }
                });
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public byte[] render() throws IOException {
        return indexSupplier.get();
    }

    @Path("/dashboard") @GET @Timed @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> defaultDashboard() {
        return ImmutableMap.of(
                "name", defaultDashboard,
                "turbine", breakerboxHostAndPort,
                "metaClusters", specifiedMetaClusters);
    }
}