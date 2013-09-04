package com.yammer.breakerbox.service.resources;

import com.google.common.base.Optional;
import com.yammer.breakerbox.service.views.DashboardView;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import com.yammer.dropwizard.authenticator.LdapAuthenticator;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {
    @GET @Timed @Produces(MediaType.TEXT_HTML)
    public DashboardView render(@Auth BasicCredentials credentials,
            @DefaultValue("production") @QueryParam("cluster") String clusterName) {
        System.out.println(credentials);
        return new DashboardView(clusterName);
    }
}
