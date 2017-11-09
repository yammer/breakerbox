package com.yammer.breakerbox.service.auth;

import com.yammer.dropwizard.authenticator.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.Collections;
import java.util.Optional;

public class NullAuthenticator implements Authenticator<BasicCredentials, User> {
    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        return Optional.of(new User("anonymous", Collections.emptySet()));
    }
}