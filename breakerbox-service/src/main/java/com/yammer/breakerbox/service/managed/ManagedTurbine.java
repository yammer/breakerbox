package com.yammer.breakerbox.service.managed;

import com.netflix.turbine.init.TurbineInit;
import io.dropwizard.lifecycle.Managed;

public class ManagedTurbine implements Managed {
    @Override
    public void start() throws Exception {
        //Start happens in an scheduled thread since it needs to have the connector live to work.
    }

    @Override
    public void stop() throws Exception {
        TurbineInit.stop();
    }
}