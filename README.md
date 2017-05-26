Breakerbox [![Build Status](https://travis-ci.org/yammer/breakerbox.svg?style=flat-square)](https://travis-ci.org/yammer/breakerbox) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.yammer.breakerbox/breakerbox-service/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.yammer.breakerbox/breakerbox-service)
==========

Breakerbox is a dashboard and dynamic configuration tool for [Tenacity](https://github.com/yammer/tenacity).

![Breakerbox Dashboard](https://yammer.github.io/tenacity/breakerbox_latest.png)
![Breakerbox Configure](https://yammer.github.io/tenacity/breakerbox_configure.png)

Running Breakerbox
==================

__Required:__ Breakerbox requires you to instrument your code with [Tenacity](https://github.com/yammer/tenacity) which is a library to aid
in resilient design of foreign dependencies.

[Download a release](https://github.com/yammer/breakerbox/releases) or build it yourself with [maven](https://github.com/yammer/breakerbox#building-breakerbox)

Extract the archive and then run the following command

```bash
java -jar breakerbox-service-X.X.X.jar server breakerbox.yml
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
  initialDelay: 0s
  delay: 60s
  
breakerboxHostAndPort: localhost:8080 #default

database:
  driverClass: org.h2.Driver
  url: jdbc:h2:mem:inMemory;DB_CLOSE_DELAY=-1
  user: breakerbox
  password: breakerboxpass
  maxSize: 10
  minSize: 2
  checkConnectionWhileIdle: true
  checkConnectionHealthWhenIdleFor: 1s

tenacityClient:
  connectionTimeout: 500ms
  timeout: 1000ms

defaultDashboard: production

instanceDiscoveryClass: com.yammer.breakerbox.turbine.YamlInstanceDiscovery #default

metaClusters: 
  - production
  - stage
  - staging

turbine:
  urlSuffix: /tenacity/metrics.stream
  clusters:
    breakerbox:
      instances:
        - localhost:8080
    production:
      clusters:
        - breakerbox

rancherDiscovery:
  serviceApiUrl: http://localhost:8080/v1/projects/1a5/services
  accessKey: 3E0F7DB0A2B601981F1B
  secretKey: fWWKGNvmuWpSngyVYHXFMSnE5cDhZWKNkVmQS8zn

marathonDiscovery:
  - marathonApiUrl: http://localhost:8080
    marathonAppPort: 12345
    marathonAppNameSpace: /exampleNameSpace
    cluster: exampleCluster

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

You can choose between using an in-memory database (h2), Postgresql, MySQL (potentially other RDBMSes) and Azure Table.

For Postgresql simply modify the `database` section to (assumes the database "breakerbox" is created):

```yaml
database:
  driverClass: org.postgresql.Driver
  url: jdbc:postgresql://localhost/breakerbox
```

For MySQL:

```yaml
database:
  driverClass: com.mysql.jdbc.Driver
  url: jdbc:mysql://localhost/breakerbox
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
```yaml
turbine: /path/to/instances.yml
```

```yaml
urlSuffix: /tenacity/metrics.stream
clusters:
breakerbox:
  instances:
    - localhost:8080
production:
  clusters:
    - breakerbox
```

* `turbine` is a path to where a `yaml` configuration file containing the instances configuration is located (this is editable at runtime allowing for dynamic rediscovery).
* `urlSuffix` defaults to `/tenacity/metrics.stream` you can alter this if you have this resource at a different location
* `clusters` is the top level item for your clusters or dashboards you wish to have available. Underneath clusters you can specify dashboard names such as `breakerbox` or `production` in this example above. Underneath those you can specify either `instances` and `clusters`. `clusters` can reference other dashboards and will include their instances. This can reference cyclic dashboards and will add instances from both. `instances` lets you specify a single instance.  

Instance Discovery Class
------------------------
Specifies the `Java` canonical class name. It defaults to the `YamlInstanceDiscovery` implementation. You can also leverage the 
`com.yammer.breakerbox.turbine.KubernetesInstanceDiscovery` , `com.yammer.breakerbox.turbine.RancherInstanceDiscovery` , `com.yammer.breakerbox.turbine.MarathonInstanceDiscovery`and classes.

To integrate with RancherInstanceDiscovery, 
    
    1. specify rancher services Api url, accessKey and secret key.
        rancherDiscovery:
            serviceApiUrl: http://localhost:8080/v1/projects/1a5/services
            accessKey: 3E0F7DB0A2B601981F1B
            secretKey: fWWKGNvmuWpSngyVYHXFMSnE5cDhZWKNkVmQS8zn
        
    2. add labels in rancher service containers:
         a. tenacity.metrics.stream.enabled: true
         b. tenacity.metrics.stream.port: 8080
         c. service.cluster.name: clusterName
        
    3. RancherInstanceDiscovery will create dashboards per service-cluster with service.cluster.name label and one aggregated production dashboard. Dashboards can be created, enabled, disabled by updating labels at runtime.

To integrate with MarathonInstanceDiscovery,
    1. specify marathon services Api url, marathonAppPort(your application port), marathonAppNameSpace and clusterName.
        marathonDiscovery:
        - marathonApiUrl: http://localhost:8080
          marathonAppPort: 12345
          marathonAppNameSpace: /exampleNameSpace
          cluster: exampleCluster

    2. MarathonInstanceDiscovery will create dashboards per unique cluster specified in the config.  MarathonDiscovery supports multiple marathon namespaces and cluster support.

Meta Clusters
-------------
These are clusters that should be in the dashboard dropdown but aren't necessarily configurable. This is used for dashboards that are made up of multiple clusters that are informational, but don't make much context in the sense of configuring one of them.

```yaml
metaClusters: 
  - production
  - stage
  - staging
```

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

`mvn test`

breakerbox-azure doesn't have a mock for Azure Table as it wouldn't be that useful, so it requires a valid Azure Table account
to run the tests. If you do not supply a valid Azure Table account, these tests will be ignored.

Docker
------
Justin Plock created a [Docker for Breakerbox](https://registry.hub.docker.com/u/jplock/breakerbox/)

Behind a Proxy
--------------
The dashboard leverages Javascript's EventSource which doesn't support CORs. There is a configurable parameter `breakerboxHostAndPort`
which defaults to `localhost:8080`. You can change this to allow for your proxy to direct traffic appropriately.
