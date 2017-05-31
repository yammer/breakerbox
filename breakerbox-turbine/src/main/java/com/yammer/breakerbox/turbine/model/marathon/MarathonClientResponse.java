
package com.yammer.breakerbox.turbine.model.marathon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarathonClientResponse {

    private App app;


    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }


}
