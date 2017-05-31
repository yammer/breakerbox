package com.yammer.breakerbox.turbine.client;

import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonClient {

    private MarathonClientConfiguration marathonClientConfiguration;

    public MarathonClient(MarathonClientConfiguration marathonClientConfiguration){
        this.marathonClientConfiguration = marathonClientConfiguration;
    }

     public Invocation.Builder getServiceInstanceDetails() {
            Client client = JerseyClientBuilder.newClient();
            WebTarget target= client.target(marathonClientConfiguration.getMarathonApiUrl() + "/v2/apps"+ marathonClientConfiguration.getMarathonAppNameSpace());
            return target.request();
    }

}
