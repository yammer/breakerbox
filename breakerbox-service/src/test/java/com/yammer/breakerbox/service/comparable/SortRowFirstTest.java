package com.yammer.breakerbox.service.comparable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.yammer.breakerbox.service.azure.DependencyEntity;
import com.yammer.breakerbox.service.core.DependencyId;
import org.junit.Test;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;


public class SortRowFirstTest {
    @Test
    public void testOrdering() throws Exception {
        final DependencyId dependencyId = DependencyId.from("foo");
        final ArrayList<DependencyEntity> items = Lists.newArrayList(
                DependencyEntity.build(dependencyId, 10),
                DependencyEntity.build(dependencyId, 8),
                DependencyEntity.build(dependencyId, 12),
                DependencyEntity.build(dependencyId, 14),
                DependencyEntity.build(dependencyId, 13),
                DependencyEntity.build(dependencyId, 200));

        final ImmutableList<DependencyEntity> sortedItems = Ordering.from(new SortRowFirst())
                .immutableSortedCopy(items);

        assertThat(sortedItems.size()).isEqualTo(items.size());
        assertThat(Long.valueOf(sortedItems.get(0).getRowKey())).isEqualTo(200);
        assertThat(Long.valueOf(sortedItems.get(1).getRowKey())).isEqualTo(14);
        assertThat(Long.valueOf(sortedItems.get(2).getRowKey())).isEqualTo(13);
        assertThat(Long.valueOf(sortedItems.get(3).getRowKey())).isEqualTo(12);
        assertThat(Long.valueOf(sortedItems.get(4).getRowKey())).isEqualTo(10);
        assertThat(Long.valueOf(sortedItems.get(5).getRowKey())).isEqualTo(8);
    }
}
