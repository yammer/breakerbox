package com.yammer.breakerbox.turbine.client;

import com.yammer.breakerbox.turbine.config.RancherInstanceConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class RancherClient {
    private final Invocation.Builder builder;
    private final RancherInstanceConfiguration instanceConfiguration;

    public RancherClient(final RancherInstanceConfiguration instanceConfiguration) {
        Objects.requireNonNull(instanceConfiguration);
        this.instanceConfiguration = instanceConfiguration;
        this.builder = createInvocationBuilder();
    }

    private Invocation.Builder createInvocationBuilder() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(createRancherServiceUrl());
        return webTarget.request().header("Authorization", getBasicAuthentication());
    }

    private String createRancherServiceUrl() {
        String filterParameters = instanceConfiguration.getQueryString();
        String apiUrl = instanceConfiguration.getServiceApiUrl();
        String serviceUrl = apiUrl.charAt(apiUrl.length() - 1) == '?' ? apiUrl : apiUrl + "?";
        return serviceUrl.replaceAll("\\s", "") + filterParameters;
    }


    private String getBasicAuthentication() {
        String token = instanceConfiguration.getAccessKey() + ":" + instanceConfiguration.getSecretKey();
        try {
            return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Cannot encode with UTF-8", ex);
        }
    }

    public Response getServiceInstanceDetails() {
        return builder.get();
    }
}
