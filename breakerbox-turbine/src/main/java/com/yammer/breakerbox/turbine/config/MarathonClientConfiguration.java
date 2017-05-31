package com.yammer.breakerbox.turbine.config;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonClientConfiguration {
    @NotEmpty
    private String marathonApiUrl;
    @NotEmpty
    private String marathonAppNameSpace;
    @NotEmpty
    private Integer marathonAppPort;
    // this is the custom clustername that you can have for the services cluster
    @NotEmpty
    private String cluster;

    public String getCluster() { return cluster; }

    public String getMarathonApiUrl() {
        return marathonApiUrl;
    }

    public String getMarathonAppNameSpace() {
        return marathonAppNameSpace;
    }

    public Integer getMarathonAppPort() {
        return marathonAppPort;
    }


    public void setMarathonApiUrl(String marathonApiUrl) {
        this.marathonApiUrl = marathonApiUrl;
    }

    public void setMarathonAppNameSpace(String marathonAppNameSpace) {
        this.marathonAppNameSpace = marathonAppNameSpace;
    }

    public void setMarathonAppPort(Integer marathonAppPort) {
        this.marathonAppPort = marathonAppPort;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
