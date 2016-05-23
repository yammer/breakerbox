package com.yammer.breakerbox.service.turbine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.netflix.config.ConfigurationManager;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import io.dropwizard.configuration.ConfigurationFactory;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class YamlInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlInstanceDiscovery.class);
    private final Path path;
    private final ConfigurationFactory<YamlInstanceConfiguration> configurationFactory;
    private final AbstractConfiguration configurationManager;

    public YamlInstanceDiscovery(Path path,
                                 Validator validator,
                                 ObjectMapper objectMapper) {
        this.path = path;
        this.configurationFactory = new ConfigurationFactory<>(
                YamlInstanceConfiguration.class,
                validator,
                objectMapper,
                "dw");
        this.configurationManager = ConfigurationManager.getConfigInstance();
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        final YamlInstanceConfiguration configuration = parseYamlInstanceConfiguration()
                .orElse(new YamlInstanceConfiguration());
        configurationManager.setProperty("turbine.instanceUrlSuffix", configuration.getUrlSuffix());
        configurationManager.setProperty(InstanceDiscovery.TURBINE_AGGREGATOR_CLUSTER_CONFIG,
                Joiner.on(',').join(configuration.getClusters().keySet()));
        return configuration.getAllInstances();
    }

    private Optional<YamlInstanceConfiguration> parseYamlInstanceConfiguration() {
        try {
            return Optional.of(configurationFactory.build(path.toFile()));
        } catch (Exception err) {
            LOGGER.error("Unable to parse {}", path.toAbsolutePath(), err);
        }
        return Optional.empty();
    }
}
