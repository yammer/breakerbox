package com.yammer.breakerbox.dashboard.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/tenacity")
public class IndexResource {
    private final Supplier<byte[]> indexSupplier;

    public IndexResource() {
        indexSupplier = Suppliers.memoize(new Supplier<byte[]>() {
            @Override
            public byte[] get() {
                try {
                    return Resources.asByteSource(Resources.getResource("assets/index.html"))
                            .read();
                } catch (IOException err) {
                    throw new IllegalStateException(err);
                }
            }
        });
    }

    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public byte[] render() throws IOException {
        return indexSupplier.get();
    }
}