package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;

public class ArchaiusOverrideConfiguration {
    @JsonProperty
    private Duration turbineHostRetry = Duration.seconds(1);
    @JsonProperty
    private int hystrixMetricsStreamServletMaxConnections = 5;
    @JsonProperty
    private Duration turbineLatencyThreshold = Duration.milliseconds(2500);
    @JsonProperty
    private Duration turbineSkipLineDelay = Duration.milliseconds(500);

    public ArchaiusOverrideConfiguration () { /* Jackson */ }

    public Duration getTurbineHostRetry() {
        return turbineHostRetry;
    }

    public void setTurbineHostRetry(Duration turbineHostRetry) {
        this.turbineHostRetry = turbineHostRetry;
    }

    public int getHystrixMetricsStreamServletMaxConnections() {
        return hystrixMetricsStreamServletMaxConnections;
    }

    public void setHystrixMetricsStreamServletMaxConnections(int hystrixMetricsStreamServletMaxConnections) {
        this.hystrixMetricsStreamServletMaxConnections = hystrixMetricsStreamServletMaxConnections;
    }

    public Duration getTurbineLatencyThreshold() {
        return turbineLatencyThreshold;
    }

    public void setTurbineLatencyThreshold(Duration turbineLatencyThreshold) {
        this.turbineLatencyThreshold = turbineLatencyThreshold;
    }

    public Duration getTurbineSkipLineDelay() {
        return turbineSkipLineDelay;
    }

    public void setTurbineSkipLineDelay(Duration turbineSkipLineDelay) {
        this.turbineSkipLineDelay = turbineSkipLineDelay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchaiusOverrideConfiguration that = (ArchaiusOverrideConfiguration) o;

        if (hystrixMetricsStreamServletMaxConnections != that.hystrixMetricsStreamServletMaxConnections) return false;
        if (!turbineHostRetry.equals(that.turbineHostRetry)) return false;
        if (!turbineLatencyThreshold.equals(that.turbineLatencyThreshold)) return false;
        if (!turbineSkipLineDelay.equals(that.turbineSkipLineDelay)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = turbineHostRetry.hashCode();
        result = 31 * result + hystrixMetricsStreamServletMaxConnections;
        result = 31 * result + turbineLatencyThreshold.hashCode();
        result = 31 * result + turbineSkipLineDelay.hashCode();
        return result;
    }
}
