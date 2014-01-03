package com.yammer.breakerbox.service.tests;

import com.google.common.io.Resources;
import com.yammer.breakerbox.azure.TableClient;
import com.yammer.breakerbox.azure.TableClientFactory;
import com.yammer.breakerbox.azure.core.TableId;
import com.yammer.breakerbox.service.config.BreakerboxServiceConfiguration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;

import java.io.File;

public abstract class AbstractTestWithConfiguration {
    protected BreakerboxServiceConfiguration breakerboxConfiguration;
    protected TableClient tableClient;

    @Before
    public void setupTest() throws Exception {
        breakerboxConfiguration = ConfigurationFactory
                .forClass(BreakerboxServiceConfiguration.class, new Validator())
                .build(new File(Resources.getResource("test.yml").toURI()));
        tableClient = new TableClientFactory(breakerboxConfiguration.getAzure()).create();

        for (TableId id : TableId.values()) {
            tableClient.create(id);
        }
    }
}
