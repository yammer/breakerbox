Breakerbox
==========

Breakerbox is a dashboard and dynamic configuration tool for [Tenacity](https://github.com/yammer/tenacity).

![Breakerbox Dashboard](https://yammer.github.io/tenacity/breakerbox_latest.png)
![Breakerbox Configure](https://yammer.github.io/tenacity/breakerbox_configure.png)

Running Breakerbox
==================

__Required:__ Breakerbox requires you to instrument your code with [Tenacity](https://github.com/yammer/tenacity) which is a library to aid
in resilient design of foreign dependencies.

Download a release: ???

Extract the archive and then run the following command

```bash
java -Darchaius.configurationSource.additionalUrls=file:config.properties -jar breakerbox-service.jar server breakerbox.yml
```

Then point your browser at ``http://localhost:8080``. You should see a dashboard
consisting of the local Breakerbox instance. By default it's backed by an in memory database, so
any configuration changes you'll make won't be persisted anywhere until your configure it.

Configuring Breakerbox
---------------------------------------

Here's a very simple bare configuration of Breakerbox. Each section will be described in detail.

```yaml
breakerbox:
  urls: file:config.properties,http://localhost:8080/archaius/breakerbox
  initialDelay: 5s
  delay: 60s

database:
  driverClass: org.h2.Driver
  url: jdbc:h2:mem:inMemory
  user: breakerbox
  password: breakerboxpass
  maxSize: 10
  minSize: 2
  checkConnectionWhileIdle: true
  checkConnectionHealthWhenIdleFor: 1s

tenacityClient:
  connectionTimeout: 500ms
  timeout: 1000ms

server:
  applicationConnectors:
    - type: http
      port: 8080
  adminConnectors:
    - type: http
      port: 8081
  gzip:
    enabled: false
  requestLog:
    appenders:
      - type: file
        currentLogFilename: /var/log/breakerbox/requests.log
        archivedLogFilenamePattern: /var/log/breakerbox/requests-%d.log
        archivedFileCount: 5

logging:
  level: INFO
  appenders:
    - type: file
      currentLogFilename: /var/log/breakerbox/breakerbox.log
      archivedLogFilenamePattern: /var/log/breakerbox/breakerbox-%d.log
      archivedFileCount: 5
```

Gzip Disabled
-------------
Javascript's EventSource doesn't support gzip encoded streams.

Persistence Storage
-------------------

You can choose between using an in-memory database, Postgresql (potentially other RDBMSes, I've just only tested with Postgresql for now), and Azure Table.

For Postgresql simply modify the `database` section to (assumes the database "breakerbox" is created):

```yaml
database:
  driverClass: org.postgresql.Driver
  url: jdbc:postgresql://localhost/breakerbox
```

If you wish to use Azure Table remove the `database` section entirely and add

```yaml
azure:
  accountName: your_test_account
  accountKey: security_key
  timeout: 2s
  retryInterval: 500ms
  retryAttempts: 1
```

If you specify both `database` and `azure` it will only leverage the `database` store.

Authentication
--------------
By default users that save configuration's will save it under the name `anonymous`.

At the moment it only supports authenticating users via LDAP and HTTP Basic-Auth which is configurable by adding:

```yaml
ldap:
  uri: ldaps://ldap.com
  cachePolicy: maximumSize = 10000, expireAfterAccess = 15m
  userFilter: ou=users,dc=company,dc=com
  userNameAttribute: cn
  connectTimeout: 500ms
  readTimeout: 500ms
```

If you need group membership filters you can see the additional documentation on [dropwizard-auth-ldap](https://github.com/yammer/dropwizard-auth-ldap)

Dashboard Configuration
-----------------------------------------
The `config.properties` file uses [Turbine's](https://github.com/Netflix/Turbine/wiki/Configuration) configuration syntax. This currently assumes a static setup. It isn't hard to add a dynamic host discovery
system, but this is something that will be added on later.

* `turbine.aggregator.clusterConfig`: specifies all the dashboards you'd like to create. (e.g. production, serviceA, serviceB)
* `turbine.instanceUrlSuffix=/tenacity/metrics.stream`: use this default because this is where `tenacity` puts the metrics streaming endpoint.
* `turbine.ConfigPropertyBasedDiscovery.production.instances`: this specifies the individual instances that make up the `production` cluster.
* `turbine.ConfigPropertyBasedDiscovery.breakerbox.instances`: this specifies the individual instances that make up the `breakerbox` cluster.

```
turbine.aggregator.clusterConfig=production,breakerbox
turbine.instanceUrlSuffix=/tenacity/metrics.stream
turbine.ConfigPropertyBasedDiscovery.production.instances=localhost:8080,anotherservice.company.com:8080
turbine.ConfigPropertyBasedDiscovery.breakerbox.instances=localhost:8080
```

*Note*: Easiest thing to do is make sure `config.properties` is in the same directory as the jar otherwise keep the `breakerbox#urls` and the `archaius.configurationSource.additionalUrls` system property pointing to the correct location.
We plan to move this to a better configuration mechanism in the future.

Adding Breakerbox to your Dropwizard+Tenacity Service
-----------------------
In any `Tenacity` service to have it poll `Breakerbox` for dynamic configurations add this to your service configuration.

* `urls`: A comma-delimited list of breakerbox instances. The cluster you added in the `config.properties` is the identifier you want to supply here.
* `initialDelay`: How long it should wait before the first poll.
* `delay`: The interval at which to constantly check `Breakerbox` for configuration items. Here we specify every minute we'd like to check for new configurations.

```yaml
breakerbox:
  urls: http://breakerbox:8080/archaius/{the-cluster-you-setup-in-breakerbox}
  initialDelay: 0s
  delay: 60s
```

There is a configuration hierarchy priority and it's good to understand it especially when using dynamic configurations.

1. Breakerbox
2. Service configuration YAML
3. Tenacity defaults

Additional Configuration
------------------------
Here's a collection of things we have found very useful to make configurable.

* `hystrixMetricsStreamServletMaxConnections`: The max number of connections any `Dropwizard+Tenacity` service will allow from Breakerbox.
* `turbineHostRetry`: The interval at which to reconnect to a failed instance. 1s may be a bit fast as a default :)

```yaml
archaiusOverride:
  turbineHostRetry: 1s
  hystrixMetricsStreamServletMaxConnections: 5
```

The client `Breakerbox` uses to fetch latest property keys and configurations is configurable by

```yaml
tenacityClient:
  connectionTimeout: 500ms
  timeout: 1000ms
```

Building Breakerbox
-------------------

`mvn clean package`

The artifact will then be available under `breakerbox-service/target`.

Running Tests
-------------
breakerbox-azure doesn't have a mock for Azure Table as it wouldn't be that useful, so it requires a valid Azure Table account
to run the tests. If you do not supply a valid Azure Table account, these tests will be ignored.
