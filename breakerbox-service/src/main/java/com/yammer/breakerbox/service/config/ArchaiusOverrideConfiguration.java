package com.yammer.breakerbox.service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;

import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(turbineHostRetry, hystrixMetricsStreamServletMaxConnections, turbineLatencyThreshold, turbineSkipLineDelay);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ArchaiusOverrideConfiguration other = (ArchaiusOverrideConfiguration) obj;
        return Objects.equals(this.turbineHostRetry, other.turbineHostRetry)
                && Objects.equals(this.hystrixMetricsStreamServletMaxConnections, other.hystrixMetricsStreamServletMaxConnections)
                && Objects.equals(this.turbineLatencyThreshold, other.turbineLatencyThreshold)
                && Objects.equals(this.turbineSkipLineDelay, other.turbineSkipLineDelay);
    }
}
