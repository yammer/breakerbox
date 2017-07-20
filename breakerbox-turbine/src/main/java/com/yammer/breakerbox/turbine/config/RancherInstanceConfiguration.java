package com.yammer.breakerbox.turbine.config;

import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import java.util.Map;

public class RancherInstanceConfiguration {
    @NotEmpty
    private String serviceApiUrl;
    @NotEmpty
    private String accessKey;
    @NotEmpty
    private String secretKey;
    @Nullable
    private Map<String, String> parameters;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getServiceApiUrl() {
        return serviceApiUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RancherInstanceConfiguration that = (RancherInstanceConfiguration) o;
      return serviceApiUrl.equals(that.serviceApiUrl)
              && accessKey.equals(that.accessKey)
              && secretKey.equals(that.secretKey)
              && null != parameters ? parameters.equals(that.parameters) : null == that.parameters;
    }

    @Override
    public int hashCode() {
        int result = serviceApiUrl.hashCode();
        result = 31 * result + accessKey.hashCode();
        result = 31 * result + secretKey.hashCode();
        if(null != parameters) {
            result = 31 * result + parameters.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "RancherInstanceConfiguration{" +
                "serviceApiUrl='" + serviceApiUrl + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", parameters='" + getQueryString() + '\'' +
                '}';
    }

    public String getQueryString() {
        return null!=parameters ? parameters.keySet().stream().map(s -> s +"="+ parameters.get(s)).reduce((s, s2) -> s+"&"+s2).orElse("") : "";
    }
}
