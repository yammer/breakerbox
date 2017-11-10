package com.yammer.breakerbox.jdbi.args;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenacityConfigurationArgumentFactory implements ArgumentFactory<TenacityConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityConfigurationArgumentFactory.class);
    private final ObjectMapper objectMapper;

    public TenacityConfigurationArgumentFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof TenacityConfiguration;
    }

    @Override
    public Argument build(Class<?> expectedType, final TenacityConfiguration value, StatementContext ctx) {
        return (position, statement, ctx1) -> {
            try {
                statement.setObject(position, objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException err) {
                LOGGER.warn("Could not write as json: {}", value, err);
                throw new IllegalArgumentException(err);
            }
        };
    }
}