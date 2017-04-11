package com.yammer.breakerbox.turbine.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.RancherInstanceDiscovery;
import com.yammer.breakerbox.turbine.client.RancherClient;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RancherInstanceDiscoveryTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private RancherClient rancherClient = mock(RancherClient.class);
    private Response response = mock(Response.class);

    private RancherInstanceDiscovery rancherInstanceDiscovery;

    @Before
    public void setUp() throws Exception {
        when(rancherClient.getServiceInstanceDetails()).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(String.class))
                .thenReturn(FixtureHelpers.fixture("fixtures/rancherClientResponse.json"));
        rancherInstanceDiscovery = new RancherInstanceDiscovery(rancherClient, MAPPER);
    }

    @Test
    public void testGetInstanceList() throws Exception {
        Collection<Instance> instanceList = rancherInstanceDiscovery.getInstanceList();
        Assert.assertEquals(Arrays.asList(
                new Instance("192.168.1.109:8081", "xyz", true),
                new Instance("192.168.1.109:8081", "production", true)),
                instanceList);
    }
}
