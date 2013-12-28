Breakerbox
==========

A dashboard of [Tenacity](https://github.com/yammer/tenacity) circuit-breakers and threadpools displayed in real-time and the ability to configure them on-the-fly.

[Tenacity](https://github.com/yammer/tenacity) is a [Dropwizard](http://www.dropwizard.io)-module that brings together [Dropwizard](http://www.dropwizard.io)+[Hystrix](https://github.com/Netflix/Hystrix).

Running Breakerbox
==================

Download the the latest uber jar here: ???

Then run the following command

```bash
java -jar breakerbox-service.jar breakerbox.yml
```

Where `breakerbox.yml` is your typical dropwizard configuration.

Simple Breakerbox Service Configuration
---------------------------------------

```yaml
breakerbox:
  urls: file:///config.properties,http://localhost:8080/archaius/breakerbox
  initialDelay: 3s
  delay: 60s

archaiusOverride:
  turbineHostRetry: 30s
  hystrixMetricsStreamServletMaxConnections: 10

azure:
  accountName: breakerbox
  accountKey: secretkey
  connectionTimeout: 500ms
  retryInterval: 1s
  retryAttempts: 1

tenacityClient:
  connectionTimeout: 500ms
  timeout: 1000ms

ldap:
  uri: ldaps://yourldap.com:636
  cachePolicy: maximumSize=10000, expireAfterAccess=10m
  securityPrincipal: cn=%s,dc=yourcompany,dc=com

http:
  port: 8080
  adminPort: 8081
  maxIdleTime: 300s
  requestLog:
    console:
      enabled: true
    file:
      enabled: false
      currentLogFilename: /var/log/breakerbox/requests.log
      archivedLogFilenamePattern: /var/log/breakerbox/requests-%d.log.gz
      archivedFileCount: 5
logging:
  level: INFO
  loggers:
    com.netflix.turbine.monitor.cluster.AggregateClusterMonitor: ERROR
    com.netflix.turbine.monitor.instance.InstanceMonitor: ERROR
    com.ning.http.client.providers.netty: WARN
  console:
    enabled: true
  file:
    enabled: false
    currentLogFilename: /var/log/breakerbox/breakerbox.log
    archivedLogFilenamePattern: /var/log/breakerbox/breakerbox-%d.log.gz
    archivedFileCount: 5
```

Simple Breakerbox Dashboard Configuration
-----------------------------------------

Along side the service configuration, you will also need to configure which [Tenacity](https://github.com/yammer/tenacity) keys you want to add to the dashboard.
The following configuration has two dashboards. One specific to breakerbox and the other more inclusive of other services within the `production` dashboard. You'll want to reference
this configuration in your service yaml `breakerbox urls`.

```
turbine.aggregator.clusterConfig=production,breakerbox
turbine.instanceUrlSuffix=/tenacity/metrics.stream
turbine.ConfigPropertyBasedDiscovery.production.instances=localhost:8080,anotherservice.company.com:8080
turbine.ConfigPropertyBasedDiscovery.breakerbox.instances=localhost:8080
```

How do I add a service to Breakerbox?
-------------------------------------
You'll want to clone the `https://github.int.yammer.com/yammer/breakerbox-configuration.git` repository and then edit
https://github.int.yammer.com/yammer/breakerbox-configuration/blob/master/modules/breakerbox/files/config.properties.
You'll want to do the two following edits:

1. Add a new `turbine.ConfigPropertyBasedDiscovery.{serviceName}.instances` where `{serviceName}` is replaced with `completie-prod` or `completie-staging` for example. Please don't forget to include
the port at which your service is running (e.g. 8080).

2. If your service addition is in the production environment, consider adding it to the meta-dashboard `turbine.ConfigPropertyBasedDiscovery.production.instances`.

3. Then add your {serviceName} to the `turbine.aggregator.clusterConfig`.

Then commit, push, repackage, and redeploy the latest version of `breakerbox`!
