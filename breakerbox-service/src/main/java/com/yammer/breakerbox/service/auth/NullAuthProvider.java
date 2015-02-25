package com.yammer.breakerbox.service.auth;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NullAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullAuthProvider.class);

    private static class BasicAuthInjectable<T> extends AbstractHttpContextInjectable<T> {
        private final Authenticator<BasicCredentials, T> authenticator;
        private final boolean required;

        private BasicAuthInjectable(Authenticator<BasicCredentials, T> authenticator, boolean required) {
            this.authenticator = authenticator;
            this.required = required;
        }

        @Override
        public T getValue(HttpContext c) {
            try {
                final Optional<T> result = authenticator.authenticate(null);
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }


            if (required) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Credentials are required to access this resource.")
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .build());
            }
            return null;
        }
    }


    private final Authenticator<BasicCredentials, T> authenticator;

    public NullAuthProvider(Authenticator<BasicCredentials, T> authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<T> getInjectable(ComponentContext ic, Auth auth, Parameter parameter) {
        return new BasicAuthInjectable<>(authenticator, auth.required());
    }
}