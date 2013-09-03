package com.yammer.breakerbox.service.config;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.net.HostAndPort;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class LdapConfiguration {
    @NotNull
    @NotEmpty
    private final String host = null;
    @NotNull @Min(value=0) @Max(value=65535)
    private final Integer port = null;
    @NotNull @Valid
    private final CacheBuilderSpec cache = null;

    private LdapConfiguration() { /* Jackson */ }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public CacheBuilderSpec getCache() {
        return cache;
    }

    public HostAndPort getHostAndPort() {
        return HostAndPort.fromParts(getHost(), getPort());
    }
}
