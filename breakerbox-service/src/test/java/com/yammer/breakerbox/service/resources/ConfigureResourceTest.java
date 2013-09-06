package com.yammer.breakerbox.service.resources;

import com.yammer.breakerbox.service.azure.ServiceEntity;
import com.yammer.breakerbox.service.core.BreakerboxStore;
import com.yammer.breakerbox.service.core.DependencyId;
import com.yammer.breakerbox.service.tests.AbstractTestWithConfiguration;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConfigureResourceTest extends AbstractTestWithConfiguration {

    //Temporary test
    @Test
    public void testConfigurePostDoubleDispatch() throws Exception {
        final BreakerboxStore breakerboxStore = mock(BreakerboxStore.class);
        final ConfigureResource configureResource = new ConfigureResource(breakerboxStore, null);

        final AtomicBoolean serviceTableInvocation = new AtomicBoolean(false);
        final AtomicBoolean dependencyTableInvocation = new AtomicBoolean(false);

        when(breakerboxStore.storeDependencyEntity(any(DependencyId.class),anyLong(), any(TenacityConfiguration.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                dependencyTableInvocation.set(true);
                return true;
            }
        });
        when(breakerboxStore.storeServiceEntity(any(ServiceEntity.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                serviceTableInvocation.set(true);
                return true;
            }
        });

        configureResource.configure(new BasicCredentials("Jimmy", "McGee"),
                "serviceName", "dependencyName", 1000, 30, 10, 1000, 1000, 10, 4, 1, -1, 10, 10, 10); //numbers relatively arbitrary

        assertTrue(serviceTableInvocation.get());
        assertTrue(dependencyTableInvocation.get());


    }
}
