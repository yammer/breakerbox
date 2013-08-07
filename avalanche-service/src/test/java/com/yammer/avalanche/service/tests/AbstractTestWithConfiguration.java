package com.yammer.avalanche.service.tests;

import com.google.common.io.Resources;
import com.yammer.avalanche.service.azure.TableId;
import com.yammer.avalanche.service.config.AvalancheConfiguration;
import com.yammer.azure.TableClient;
import com.yammer.azure.TableClientFactory;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;

import java.io.File;

public abstract class AbstractTestWithConfiguration {
    protected AvalancheConfiguration configuration;
    protected TableClient tableClient;

    @Before
    public void setupTest() throws Exception {
        configuration = ConfigurationFactory
                .forClass(AvalancheConfiguration.class, new Validator())
                .build(new File(Resources.getResource("test.yml").toURI()));
        tableClient = new TableClientFactory(configuration.getAzure()).create();

        for (TableId id : TableId.values()) {
            tableClient.create(id);
        }
    }
}
