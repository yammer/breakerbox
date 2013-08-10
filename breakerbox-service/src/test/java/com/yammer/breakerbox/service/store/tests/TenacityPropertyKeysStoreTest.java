package com.yammer.breakerbox.service.store.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.yammer.breakerbox.service.store.TenacityPropertyKeysStore;
import com.yammer.breakerbox.service.tenacity.TenacityPoller;
import org.junit.Test;

import java.net.URI;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TenacityPropertyKeysStoreTest {
    @Test
    public void tenacityPropertyKeysForUris() {
        final TenacityPoller.Factory mockFactory = mock(TenacityPoller.Factory.class);
        final TenacityPoller mockPoller = mock(TenacityPoller.class);
        final TenacityPoller mockPoller2 = mock(TenacityPoller.class);
        final URI firstUri = URI.create("http://localhost:1234");
        final URI secondUri = URI.create("http://another:8080");

        when(mockFactory.create(firstUri)).thenReturn(mockPoller);
        when(mockFactory.create(secondUri)).thenReturn(mockPoller2);
        when(mockPoller.execute()).thenReturn(Optional.of(ImmutableList.of("things", "stuff")));
        when(mockPoller2.execute()).thenReturn(Optional.of(ImmutableList.of("morethings", "stuff")));

        final TenacityPropertyKeysStore tenacityPropertyKeysStore = new TenacityPropertyKeysStore(mockFactory);

        assertThat(tenacityPropertyKeysStore.tenacityPropertyKeysFor(ImmutableList.of(firstUri, secondUri)))
                .isEqualTo(ImmutableSet.of("things", "stuff", "morethings"));
    }
}
