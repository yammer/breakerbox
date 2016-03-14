package com.yammer.breakerbox.service.views;

import com.yammer.breakerbox.service.core.Instances;
import io.dropwizard.views.View;

import java.util.Set;

public abstract class NavbarView extends View {
    
    private Set<String> specifiedMetaClusters;

    protected NavbarView(String templateName, Set<String> specifiedMetaClusters) {
        super(templateName);
        this.specifiedMetaClusters = specifiedMetaClusters;
    }

    public Iterable<String> getClusters() {
        return Instances.clusters();
    }

    public Iterable<String> getNoMetaClusters() {
        return Instances.noMetaClusters(specifiedMetaClusters);
    }
}