package com.yammer.breakerbox.turbine.tests;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.RancherInstanceDiscovery;
import com.yammer.breakerbox.turbine.client.RancherClient;
import com.yammer.breakerbox.turbine.config.RancherInstanceConfiguration;

import io.dropwizard.testing.FixtureHelpers;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {RancherInstanceDiscovery.class})
public class RancherInstanceDiscoveryTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private RancherInstanceConfiguration rancherInstanceConfiguration;
    @Mock
    private RancherClient rancherClient;
    @Mock
    private Response response;

    private RancherInstanceDiscovery rancherInstanceDiscovery;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(RancherClient.class).withAnyArguments().thenReturn(rancherClient);
        Mockito.when(rancherClient.getServiceInstanceDetails()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.readEntity(String.class))
                .thenReturn(FixtureHelpers.fixture("fixtures/rancherClientResponse.json"));
        this.rancherInstanceDiscovery = new RancherInstanceDiscovery(rancherInstanceConfiguration, MAPPER);
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
