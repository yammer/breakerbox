package com.yammer.breakerbox.jdbi.args;

import com.yammer.breakerbox.store.DependencyId;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class DependencyIdArgumentFactory implements ArgumentFactory<DependencyId> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof DependencyId;
    }

    @Override
    public Argument build(Class<?> expectedType, final DependencyId value, StatementContext ctx) {
        return (position, statement, ctx1) -> statement.setObject(position, value.getId());
    }
}