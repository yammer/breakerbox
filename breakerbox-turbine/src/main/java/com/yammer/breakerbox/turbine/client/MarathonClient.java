package com.yammer.breakerbox.turbine.client;

import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonClient {
    private final Invocation.Builder builder;
    private final MarathonClientConfiguration marathonClientConfiguration;

    public MarathonClient(final MarathonClientConfiguration marathonClientConfiguration){
        this.marathonClientConfiguration = marathonClientConfiguration;
        this.builder = createInvocationBuilder();
    }

    private Invocation.Builder createInvocationBuilder() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(marathonClientConfiguration.getMarathonApiUrl()+ marathonClientConfiguration.getMarathonAppNameSpace());
        return webTarget.request();
    }

    public Response getServiceInstanceDetails() {
        return builder.get();
    }
}
