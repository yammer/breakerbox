package com.yammer.breakerbox.turbine.config;

import org.hibernate.validator.constraints.NotEmpty;

public class RancherInstanceConfiguration {
    @NotEmpty
    private String serviceApiUrl;
    @NotEmpty
    private String accessKey;
    @NotEmpty
    private String secretKey;

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
              && secretKey.equals(that.secretKey);
    }

    @Override
    public int hashCode() {
        int result = serviceApiUrl.hashCode();
        result = 31 * result + accessKey.hashCode();
        result = 31 * result + secretKey.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RancherInstanceConfiguration{" +
                "serviceApiUrl='" + serviceApiUrl + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                '}';
    }
}
