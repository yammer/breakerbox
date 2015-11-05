package com.yammer.breakerbox.service.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

public class NullAuthFilter<P extends Principal> extends AuthFilter<BasicCredentials, P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullAuthFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        try {
            final Optional<P> principal = authenticator.authenticate(null);
            if (principal.isPresent()) {
                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return principal.get();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return authorizer.authorize(principal.get(), role);
                    }

                    @Override
                    public boolean isSecure() {
                        return requestContext.getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }
                });
                return;
            }
        } catch (AuthenticationException e) {
            LOGGER.warn("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error decoding credentials", e);
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    public static class Builder<P extends Principal> extends
            AuthFilterBuilder<BasicCredentials, P, NullAuthFilter<P>> {

        @Override
        protected NullAuthFilter<P> newInstance() {
            return new NullAuthFilter<>();
        }
    }
}