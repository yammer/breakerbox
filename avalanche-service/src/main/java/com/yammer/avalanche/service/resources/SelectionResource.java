package com.yammer.avalanche.service.resources;

import com.yammer.metrics.annotation.Timed;
import com.yammer.avalanche.service.views.SelectionView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class SelectionResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public SelectionView render() {
        return new SelectionView();
    }
}