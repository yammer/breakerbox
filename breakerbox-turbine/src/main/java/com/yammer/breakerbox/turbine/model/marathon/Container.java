
package com.yammer.breakerbox.turbine.model.marathon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Container {

    private Docker docker;

    public Docker getDocker() {
        return docker;
    }

    public void setDocker(Docker docker) {
        this.docker = docker;
    }


}
