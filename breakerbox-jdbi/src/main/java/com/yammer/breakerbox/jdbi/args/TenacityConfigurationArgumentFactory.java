package com.yammer.breakerbox.jdbi.args;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.tenacity.core.config.TenacityConfiguration;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TenacityConfigurationArgumentFactory implements ArgumentFactory<TenacityConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenacityConfigurationArgumentFactory.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperFactory().build();

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof TenacityConfiguration;
    }

    @Override
    public Argument build(Class<?> expectedType, final TenacityConfiguration value, StatementContext ctx) {
        return new Argument() {
            @Override
            public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
                try {
                    statement.setObject(position, OBJECT_MAPPER.writeValueAsString(value));
                } catch (JsonProcessingException err) {
                    LOGGER.warn("Could not write as json: {}", value, err);
                    throw new IllegalArgumentException(err);
                }
            }
        };
    }
}