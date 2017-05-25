package com.yammer.breakerbox.turbine.client;

import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonClient {

    private MarathonClientConfiguration marathonClientConfiguration;

    public MarathonClient(MarathonClientConfiguration marathonClientConfiguration){
        this.marathonClientConfiguration = marathonClientConfiguration;
    }

     public Response getServiceInstanceDetails() {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(marathonClientConfiguration.getMarathonApiUrl() + marathonClientConfiguration   .getMarathonAppNameSpace());
            return webTarget.request().get();

    }

}
