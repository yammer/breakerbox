package com.yammer.breakerbox.service.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

public class NullAuthenticator implements Authenticator<BasicCredentials, BasicCredentials> {
    @Override
    public Optional<BasicCredentials> authenticate(BasicCredentials credentials) throws AuthenticationException {
        return Optional.of(new BasicCredentials("anonymous", ""));
    }
}