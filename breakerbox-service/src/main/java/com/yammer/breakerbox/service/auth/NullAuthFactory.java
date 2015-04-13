package com.yammer.breakerbox.service.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.*;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

public class NullAuthFactory<T> extends AuthFactory<BasicCredentials, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullAuthFactory.class);

    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm = "Null";
    private final String prefix = "Null";
    private UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

    @Context
    private HttpServletRequest request;

    public NullAuthFactory(final Authenticator<BasicCredentials, T> authenticator,
                           final Class<T> generatedClass) {
        super(authenticator);
        this.required = false;
        this.generatedClass = generatedClass;
    }

    private NullAuthFactory(final boolean required,
                            final Authenticator<BasicCredentials, T> authenticator,
                            final Class<T> generatedClass) {
        super(authenticator);
        this.required = required;
        this.generatedClass = generatedClass;
    }

    public NullAuthFactory<T> responseBuilder(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        return this;
    }

    @Override
    public AuthFactory<BasicCredentials, T> clone(boolean required) {
        return new NullAuthFactory<>(required, authenticator(), this.generatedClass).responseBuilder(unauthorizedHandler);
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public T provide() {
        if (request != null) {
            try {
                final Optional<T> result = authenticator().authenticate(null);
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
        }

        if (required) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        return null;
    }

    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}