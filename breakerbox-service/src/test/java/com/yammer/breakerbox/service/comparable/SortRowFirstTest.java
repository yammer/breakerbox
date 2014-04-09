package com.yammer.breakerbox.service.comparable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.yammer.breakerbox.azure.model.DependencyEntity;
import com.yammer.breakerbox.store.DependencyId;
import com.yammer.breakerbox.store.ServiceId;
import com.yammer.breakerbox.store.model.DependencyModel;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


public class SortRowFirstTest {
    @Test
    public void testOrdering() throws Exception {
        final DependencyId dependencyId = DependencyId.from("foo");
        final ServiceId serviceId = ServiceId.from("testService");
        final ImmutableList<DependencyModel> items = ImmutableList.of(
                new DependencyModel(dependencyId, new DateTime(10), DependencyEntity.defaultConfiguration(), "testUser", serviceId),
                new DependencyModel(dependencyId, new DateTime(8), DependencyEntity.defaultConfiguration(), "testUser", serviceId),
                new DependencyModel(dependencyId, new DateTime(12), DependencyEntity.defaultConfiguration(), "testUser", serviceId),
                new DependencyModel(dependencyId, new DateTime(14), DependencyEntity.defaultConfiguration(), "testUser", serviceId),
                new DependencyModel(dependencyId, new DateTime(13), DependencyEntity.defaultConfiguration(), "testUser", serviceId),
                new DependencyModel(dependencyId, new DateTime(200), DependencyEntity.defaultConfiguration(), "testUser", serviceId));

        final ImmutableList<DependencyModel> sortedItems = Ordering.from(new SortRowFirst())
                .reverse()
                .immutableSortedCopy(items);

        assertThat(sortedItems.size()).isEqualTo(items.size());
        assertThat(sortedItems.get(0).getDateTime()).isEqualTo(new DateTime(200));
        assertThat(sortedItems.get(1).getDateTime()).isEqualTo(new DateTime(14));
        assertThat(sortedItems.get(2).getDateTime()).isEqualTo(new DateTime(13));
        assertThat(sortedItems.get(3).getDateTime()).isEqualTo(new DateTime(12));
        assertThat(sortedItems.get(4).getDateTime()).isEqualTo(new DateTime(10));
        assertThat(sortedItems.get(5).getDateTime()).isEqualTo(new DateTime(8));
    }
}
