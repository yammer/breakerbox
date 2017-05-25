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

    public String getMarathonApiUrl() {
        return marathonApiUrl;
    }

    public String getMarathonAppNameSpace() {
        return marathonAppNameSpace;
    }

    public Integer getMarathonAppPort() {
        return marathonAppPort;
    }

    @Override
    public String toString() {
        return "MarathonClientConfiguration{" +
                "marathonApiUrl='" + marathonApiUrl + '\'' +
                ", marathonAppNameSpace='" + marathonAppNameSpace + '\'' +
                ", marathonAppPort='" + marathonAppPort + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarathonClientConfiguration that = (MarathonClientConfiguration) o;

        if (marathonApiUrl != null ? !marathonApiUrl.equals(that.marathonApiUrl) : that.marathonApiUrl != null)
            return false;
        if (marathonAppNameSpace != null ? !marathonAppNameSpace.equals(that.marathonAppNameSpace) : that.marathonAppNameSpace != null)
            return false;
        return marathonAppPort != null ? marathonAppPort.equals(that.marathonAppPort) : that.marathonAppPort == null;

    }

    @Override
    public int hashCode() {
        int result = marathonApiUrl != null ? marathonApiUrl.hashCode() : 0;
        result = 31 * result + (marathonAppNameSpace != null ? marathonAppNameSpace.hashCode() : 0);
        result = 31 * result + (marathonAppPort != null ? marathonAppPort.hashCode() : 0);
        return result;
    }


}
