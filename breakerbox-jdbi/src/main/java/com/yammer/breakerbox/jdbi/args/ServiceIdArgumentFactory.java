package com.yammer.breakerbox.jdbi.args;

import com.yammer.breakerbox.store.ServiceId;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class ServiceIdArgumentFactory implements ArgumentFactory<ServiceId> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof ServiceId;
    }

    @Override
    public Argument build(Class<?> expectedType, final ServiceId value, StatementContext ctx) {
        return (position, statement, ctx1) -> statement.setObject(position, value.getId());
    }
}