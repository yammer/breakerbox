package com.yammer.breakerbox.azure.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import com.yammer.breakerbox.azure.AzureTableConfiguration;
import com.yammer.breakerbox.azure.TableClientFactory;
import com.yammer.breakerbox.azure.core.TableId;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.junit.Before;

import javax.validation.Validation;
import java.io.File;

public abstract class WithConfiguration {
    protected AzureTableConfiguration azureTableConfiguration;

    @Before
    public void setupTest() throws Exception {
        azureTableConfiguration = new YamlConfigurationFactory<>(
                AzureTableConfiguration.class,
                Validation.buildDefaultValidatorFactory().getValidator(),
                Jackson.newObjectMapper(),
                "dw.").build(new File(Resources.getResource("azure-test.yml").toURI()));
    }

    protected boolean validAzureAccount() {
        try {
            new TableClientFactory(azureTableConfiguration).create().create(TableId.SERVICE);
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    protected static Environment environment() {
        return new Environment(
                "test",
                Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(),
                new MetricRegistry(),
                Thread.currentThread().getContextClassLoader());
    }
}