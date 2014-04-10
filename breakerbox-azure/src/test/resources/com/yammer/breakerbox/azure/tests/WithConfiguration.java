package com.yammer.breakerbox.azure.tests;

import com.google.common.io.Resources;
import com.yammer.breakerbox.azure.AzureTableConfiguration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;

import java.io.File;

public abstract class WithConfiguration {
    protected AzureTableConfiguration azureTableConfiguration;

    @Before
    public void setupTest() throws Exception {
        azureTableConfiguration = ConfigurationFactory
                .forClass(AzureTableConfiguration.class, new Validator())
                .build(new File(Resources.getResource("azure-test.yml").toURI()));
    }
}