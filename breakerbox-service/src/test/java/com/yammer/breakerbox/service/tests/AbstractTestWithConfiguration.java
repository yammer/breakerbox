package com.yammer.breakerbox.service.tests;

import com.google.common.io.Resources;
import com.yammer.azure.TableClient;
import com.yammer.azure.TableClientFactory;
import com.yammer.breakerbox.service.azure.TableId;
import com.yammer.breakerbox.service.config.BreakerboxConfiguration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;

import java.io.File;

public abstract class AbstractTestWithConfiguration {
    protected BreakerboxConfiguration configuration;
    protected TableClient tableClient;

    @Before
    public void setupTest() throws Exception {
        configuration = ConfigurationFactory
                .forClass(BreakerboxConfiguration.class, new Validator())
                .build(new File(Resources.getResource("test.yml").toURI()));
        tableClient = new TableClientFactory(configuration.getAzure()).create();

        for (TableId id : TableId.values()) {
            tableClient.create(id);
        }
    }
}
