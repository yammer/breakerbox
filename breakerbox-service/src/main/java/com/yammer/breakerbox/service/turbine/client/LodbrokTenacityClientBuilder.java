package com.yammer.breakerbox.service.turbine.client;

import com.yammer.lodbrok.discovery.core.tenacity.LodbrokTenacityClient;
import com.yammer.tenacity.core.http.TenacityJerseyClientBuilder;
import com.yammer.tenacity.core.properties.TenacityPropertyKey;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;

import javax.ws.rs.client.Client;

public class LodbrokTenacityClientBuilder {
    protected JerseyClientConfiguration jerseyConfiguration = new JerseyClientConfiguration();
    protected final Environment environment;
    protected final TenacityPropertyKey tenacityPropertyKey;

    public LodbrokTenacityClientBuilder(Environment environment,
                                        TenacityPropertyKey tenacityPropertyKey) {
        this.environment = environment;
        this.tenacityPropertyKey = tenacityPropertyKey;
    }

    public LodbrokTenacityClientBuilder using(JerseyClientConfiguration jerseyConfiguration) {
        this.jerseyConfiguration = jerseyConfiguration;
        return this;
    }

    public LodbrokTenacityClient build() {
        final Client client = new JerseyClientBuilder(environment)
                .using(jerseyConfiguration)
                .build("tenacity-" + tenacityPropertyKey);
        return new LodbrokTenacityClient(environment.metrics(), TenacityJerseyClientBuilder
                .builder(tenacityPropertyKey)
                .build(client));
    }
}
