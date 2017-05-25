
package com.yammer.breakerbox.turbine.model;

import java.util.List;

public class Docker {

    private List<PortMapping> portMappings = null;

    public List<PortMapping> getPortMappings() {
        return portMappings;
    }

    public void setPortMappings(List<PortMapping> portMappings) {
        this.portMappings = portMappings;
    }

}
