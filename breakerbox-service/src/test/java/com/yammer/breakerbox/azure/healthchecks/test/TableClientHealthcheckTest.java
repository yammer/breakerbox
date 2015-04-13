package com.yammer.breakerbox.azure.healthchecks.test;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableList;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.azure.healthchecks.TableClientHealthcheck;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableClientHealthcheckTest {

    @Test
    public void healthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.of(TableId.SERVICE.toString()));
        assertThat(tableClientHealthcheck.check()).isEqualTo(HealthCheck.Result.healthy());
    }

    @Test
    public void unhealthy() throws Exception {
        final TableClient mockTableClient = mock(TableClient.class);
        final TableClientHealthcheck tableClientHealthcheck = new TableClientHealthcheck(mockTableClient);

        when(mockTableClient.listTables()).thenReturn(ImmutableList.<String>of());
        assertThat(tableClientHealthcheck.check()).isEqualTo(HealthCheck.Result.unhealthy("Could not list tables"));
    }
}