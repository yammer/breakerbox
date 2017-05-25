package com.yammer.breakerbox.turbine.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.yammer.breakerbox.turbine.MarathonInstanceDiscovery;
import com.yammer.breakerbox.turbine.client.MarathonClient;
import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;
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

/**
 * Created by supreeth.vp on 24/05/17.
 */
public class MarathonInstanceDiscoveryTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private MarathonClient marathonClient = mock(MarathonClient.class);
    private Response response = mock(Response.class);
    private MarathonClientConfiguration marathonClientConfiguration = mock(MarathonClientConfiguration.class);
    private MarathonInstanceDiscovery marathonInstanceDiscovery;

    @Before
    public void setUp() throws Exception {
        when(marathonClient.getServiceInstanceDetails()).thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(String.class))
                .thenReturn(FixtureHelpers.fixture("fixtures/marathonClientResponse.json"));
        when(marathonClientConfiguration.getMarathonAppPort()).thenReturn(8080);
        when(marathonClientConfiguration.getMarathonAppNameSpace()).thenReturn("production");
        marathonInstanceDiscovery = new MarathonInstanceDiscovery(marathonClient, MAPPER, marathonClientConfiguration);
    }

    @Test
    public void testGetInstanceList() throws Exception {
        Collection<Instance> instanceList = marathonInstanceDiscovery.getInstanceList();
        Assert.assertEquals(Arrays.asList(
                new Instance("msr-apps4.prod-ola-dcos.olacabs.net:28083", "production", true)),
                instanceList);
    }
}
