package com.yammer.breakerbox.turbine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import io.dropwizard.configuration.ConfigurationFactory;
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

    public YamlInstanceDiscovery(Path path,
                                 Validator validator,
                                 ObjectMapper objectMapper) {
        this.path = path;
        this.configurationFactory = new ConfigurationFactory<>(
                YamlInstanceConfiguration.class,
                validator,
                objectMapper,
                "dw");
        parseYamlInstanceConfiguration();
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        return parseYamlInstanceConfiguration()
                .orElse(new YamlInstanceConfiguration())
                .getAllInstances();
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
