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

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Created by supreeth.vp on 24/05/17.
 */
public class MarathonInstanceDiscoveryTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private MarathonClient marathonClient = mock(MarathonClient.class);
    private MarathonClientConfiguration marathonClientConfiguration;
    private MarathonInstanceDiscovery marathonInstanceDiscovery;

    @Before
    public void setUp() throws Exception {

        marathonClientConfiguration = new MarathonClientConfiguration();
        marathonClientConfiguration.setMarathonAppPort(8080);
        marathonClientConfiguration.setCluster("production");
        marathonClientConfiguration.setMarathonAppNameSpace("xyz");
        marathonInstanceDiscovery = new MarathonInstanceDiscovery( MAPPER, Arrays.asList(marathonClientConfiguration));
    }

    @Test
    public void testCreateServiceInstanceList() throws Exception {
        List<Instance> instanceList = marathonInstanceDiscovery.createServiceInstanceList(FixtureHelpers.fixture("fixtures/marathonClientResponse.json"),marathonClientConfiguration);
        Assert.assertEquals(Arrays.asList(
                new Instance("xyz.net:28083", "production", true)),
                instanceList);
    }
}
