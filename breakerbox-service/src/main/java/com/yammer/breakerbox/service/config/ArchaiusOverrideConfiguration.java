package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArchaiusOverrideConfiguration {
    @JsonProperty
    private int turbineHostRetryMillis = 1000;
    @JsonProperty
    private int hystrixMetricsStreamServletMaxConnections = 5;

    public ArchaiusOverrideConfiguration () { /* Jackson */ }

    public int getTurbineHostRetryMillis() {
        return turbineHostRetryMillis;
    }

    public void setTurbineHostRetryMillis(int turbineHostRetryMillis) {
        this.turbineHostRetryMillis = turbineHostRetryMillis;
    }

    public int getHystrixMetricsStreamServletMaxConnections() {
        return hystrixMetricsStreamServletMaxConnections;
    }

    public void setHystrixMetricsStreamServletMaxConnections(int hystrixMetricsStreamServletMaxConnections) {
        this.hystrixMetricsStreamServletMaxConnections = hystrixMetricsStreamServletMaxConnections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchaiusOverrideConfiguration that = (ArchaiusOverrideConfiguration) o;

        if (hystrixMetricsStreamServletMaxConnections != that.hystrixMetricsStreamServletMaxConnections) return false;
        if (turbineHostRetryMillis != that.turbineHostRetryMillis) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = turbineHostRetryMillis;
        result = 31 * result + hystrixMetricsStreamServletMaxConnections;
        return result;
    }
}
