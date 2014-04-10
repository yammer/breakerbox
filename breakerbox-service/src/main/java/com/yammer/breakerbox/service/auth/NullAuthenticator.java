package com.yammer.breakerbox.service.auth;

import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;

public class NullAuthenticator implements Authenticator<BasicCredentials, BasicCredentials> {
    @Override
    public Optional<BasicCredentials> authenticate(BasicCredentials credentials) throws AuthenticationException {
        return Optional.of(new BasicCredentials("anonymous", ""));
    }
}