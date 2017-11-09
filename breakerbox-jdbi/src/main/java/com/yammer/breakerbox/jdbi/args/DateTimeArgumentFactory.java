package com.yammer.breakerbox.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class DateTimeArgumentFactory implements ArgumentFactory<DateTime> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof DateTime;
    }

    @Override
    public Argument build(Class<?> expectedType, final DateTime value, StatementContext ctx) {
        return (position, statement, ctx1) -> statement.setObject(position, value.getMillis());
    }
}