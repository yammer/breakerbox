package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.views.SelectionView;
import com.yammer.metrics.annotation.Timed;

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