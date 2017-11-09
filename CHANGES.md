0.6.5 -- TBD
---
* [Tenacity 1.2.0](https://github.com/yammer/tenacity/releases/tag/tenacity-parent-1.2.0)
* [Dropwizard 1.2.0](https://github.com/dropwizard/dropwizard/releases/tag/v1.2.0)
* PostgreSQL 42.1.4
* MySQL 5.1.44
* Mockito-core 2.11.0
* Azure-storage 6.1.0
* [Support also other databases, not only H2 and PostgreSQL](https://github.com/yammer/breakerbox/pull/45) Thanks @maksymgendin
* [Dependencies / Java8](https://github.com/yammer/breakerbox/pull/46)

0.6.4 -- Aug. 4, 2017
---
* [Removed hardcoding of rancher query parameters](https://github.com/yammer/breakerbox/pull/42) Thanks @priyadarsh
* [Dropwizard 1.1.3](https://github.com/dropwizard/dropwizard/releases/tag/v1.1.3)
* [azure-storage-java 5.4.0](https://github.com/Azure/azure-storage-java/releases/tag/v5.4.0)

0.6.3 -- July 13, 2017
---
* [Dropwizard 1.1.2](https://github.com/dropwizard/dropwizard/releases/tag/v1.1.2)
* Mockito-core 2.8.47
* MySQL 5.1.42
* H2 1.4.196
* PostgreSQL 42.1.2
* [Dropwizard-auth-ldap 1.0.5](https://github.com/yammer/dropwizard-auth-ldap/blob/master/CHANGELOG.md#105---may-26-2017)
* [Tenacity 1.1.3](https://github.com/yammer/tenacity/releases/tag/tenacity-parent-1.1.3)
* [Custom path for hystrix metrics stream and environment variable substitution](https://github.com/yammer/breakerbox/pull/40) Thanks @priyadarsh

0.6.2 -- May 31, 2017
---
* [MySQL Support](https://github.com/yammer/breakerbox/pull/33) Thanks @sriniprash!
* [Marathon Discovery](https://github.com/yammer/breakerbox/pull/37) Thanks @supreethnag!

0.6.1 - May 22, 2017
---
* [Dropwizard 1.1.1](https://github.com/dropwizard/dropwizard/releases/tag/v1.1.1)
* [Tenacity 1.1.2](https://github.com/yammer/tenacity/releases/tag/tenacity-parent-1.1.2)
* Mockito 2.8.9
* H2 1.4.195

0.6.0 - Apr. 11, 2017
---
* [Dropwizard 1.1.0](http://www.dropwizard.io/1.1.0/docs/about/release-notes.html)
* [Tenacity 1.1.1](https://github.com/yammer/tenacity/blob/master/CHANGES.md)
* Dropped dependency on PowerMock
* Mockito 2.7.22
* PostgreSQL 42.0.0
* [Dropwizard-auth-ldap 1.0.4](https://github.com/yammer/dropwizard-auth-ldap)
* H2 1.4.194

0.5.6 - Dec. 21, 2016
---
* [Dropwizard 1.0.5](http://www.dropwizard.io/1.0.5/docs/about/release-notes.html)
* [Tenacity 1.0.5](https://github.com/yammer/tenacity/releases/tag/tenacity-parent-1.0.5)
* PostgreSQL 9.4.1212.jre7
* Added [Google Error-Prone](https://github.com/google/error-prone) to the build

0.5.5 - Nov. 7, 2016
---
* [Dropwizard 1.0.3](http://www.dropwizard.io/1.0.3/docs/about/release-notes.html)
* [Tenacity 1.0.4](https://github.com/yammer/tenacity/blob/master/CHANGES.md)
* H2 1.4.193

0.5.4 - Oct. 10, 2016
---
* [Dropwizard-auth-ldap 1.0.2](https://github.com/yammer/dropwizard-auth-ldap)
* [Tenacity 1.0.3](https://github.com/yammer/tenacity/blob/master/CHANGES.md)
* Rancher Instance Discovery support (thanks @gauravic08)[Pull Request 29](https://github.com/yammer/breakerbox/pull/29)

0.5.3 - Sept. 28, 2016
---
* [Dropwizard 1.0.2](http://www.dropwizard.io/1.0.2/docs/about/release-notes.html#v1-0-2-sep-23-2016)
* [Tenacity 1.0.2](https://github.com/yammer/tenacity/blob/master/CHANGES.md)
* PostgreSQL 9.4.1211.jre7

0.5.2 - August 18, 2016
---
* `Hystrix 1.5.4` changed the `HystrixMetricsStreamServlet` to use a new parameter to
control the max number of concurrent connections: `hystrix.config.stream.maxConcurrentConnections`.

0.5.1 - August 9, 2016
---
* [Tenacity 1.0.1](https://github.com/yammer/tenacity)

0.5.0 - July 28, 2016
---
* [Dropwizard 1.0](http://www.dropwizard.io/1.0.0/docs/)
* Tenacity 1.0.0
* PostgreSQL 9.4.1209.jre7
* Dropwizard-auth-ldap 1.0.0
* Maven-findbugs 3.0.4  
* Kubernetes-api 2.2.144

0.4.5 - July 1, 2016
---
* Make cluster registration part of the normal cycle for `InstanceDiscovery`.

0.4.4 - June 29, 2016
---
* `Dropwizard 0.9.3`
* `Tenacity 0.8.3`
* Migrate `Instances` from `FluentIterable` to Java8 `Streams`
* Allow for custom `InstanceDiscovery` implementations
* [KubernetesInstanceDiscovery](https://github.com/yammer/breakerbox/pull/23)

0.4.3 - May 23, 2016
---
* `turbine` configuration now references a `yaml` containing the instances configuration which will be reloaded whenever there is a change.

0.4.2 - Apr 19, 2016
---
* Java 8 required
* Dropped `config.properties` and introduced `YamlInstanceDiscovery` which now lets users configure which dashboards/clusters/instances configured via `yaml`. This adds the ability to construct
  clusters made up of other clusters and instances for easy configuration.

0.4.1 - Apr 14, 2016
---
* Tenacity 0.8.2

0.4.0 - Apr 14, 2016
---
* Tenacity 0.8.1
* Rewritten UI in React.js
* Added `/clusters`, `/clusters/:service/propertykeys`, `/clusters/:service/configurations/:propertykey`

0.3.4 - Mar 17, 2016
---
* Tenacity 0.8.0
* Postgresql 9.4.1208.jre7
* H2 1.4.191
* [Deprecated elements should have both the annotation and the Javadoc tag](https://github.com/yammer/breakerbox/pull/12)
* Allow for configuration of meta clusters [Issue 10](https://github.com/yammer/breakerbox/issues/10)

0.3.3 - Jan 21, 2016
---
* Dropwizard 0.9.2
* Tenacity 0.7.3
* Postgresql 9.4.1207.jre7

0.3.2 - Nov 23, 2015
---
* Tenacity 0.7.2
* Merge dashboard fixes https://github.com/Netflix/Hystrix/pull/959

0.3.1 - Nov 5, 2015
---
* Dropwizard 0.9.1
* Tenacity 0.7.0
* dropwizard-auth-ldap 0.3.0
* Upgraded maven-plugins
* Postgresql 9.4-1205-jdbc42
* H2 1.4.190

0.3.0 - Nov 5, 2015
---
* Accidental re-release of 0.2.12

0.2.12
---
* Tenacity 0.6.16
* Fixing dashboard issue where when not all circuits were open it could cause the circuit to not render properly.

0.2.11
---
* Tenacity 0.6.15
* Merge dashboard fixes https://github.com/Netflix/Hystrix/pull/919
* Merge dashboard fixes https://github.com/Netflix/Hystrix/pull/921

0.2.10
---
* Tenacity 0.6.14
* H2 1.4.189
* Postgresql 9.4-1203-jdbc42
* Fixes a bug in `config.properties` it would not respect an instance:port:path when referencing an instance. It would only honor the instance:port and not the path.
  Now if you specify 10.0.0.1:8080/some/root, it will look for tenacity resources at 10.0.0.1:8080/some/root/tenacity/...

0.2.9
---
* Dropwizard 0.8.4
* Postgresql 9.4-1202-jdbc42
* Tenacity 0.6.13
* Dropwizard-auth-ldap 0.2.1

0.2.8
---
* Tenacity 0.6.12
* Only advertise `Archaius` values for `executionIsolationStrategy` if it was explicitly set by a user. This allows for the specific type of command `TenacityObservableCommand` or the original `TenacityCommand` to determine
  what kind of isolation strategy to use. Only override this behavior if the user sets it on their own.

0.2.7
----
* Tenacity 0.6.10
* Adding Bad Request Counts to the dashboard

0.2.6
----
* Tenacity 0.6.9
* Switch from using `latency_execute` to `latency_total` for percentile metrics. The `latency_execute` is a closer measurement to successful execution times. Where `latency_total` is more accurate at the actual time the application is experiencing for successful and timed out commands. Since we assume breakerbox is used more in a debugging response and investigations rather than profiling/benchmarking, `latency_total` is more helpful in those quick response situations. In comparison when evaluating if your threadpool sizes and timeouts are set appropriately it is better to look at a historical trend of the `latency_execute` metric.

0.2.5
-----
* Tenacity 0.6.7
* Dropwizard-auth-ldap 0.2.0

0.2.4
-----
* Fixed issue where Breakerbox incorrectly configured `Turbine`.

~~0.2.3~~ Broke the initial configuration
-----
* Tenacity 0.6.4
* H2 1.4.187

0.2.2
-----
* Tenacity 0.6.3. Resolved the memory-leak in `TenacityJerseyClient` by leveraging a workaround.

0.2.1
-----
* Removing use of `TenacityJerseyClient`. We have identified a memory-leak with its implementation when using Jersey 2.x and dropwizard 0.8.x

0.2.0
-----
* Dropwizard 0.8.1
* Tenacity 0.6.2
* Timed out metrics on dashboard for semaphore commands
* Ability to live configure `executionIsolationStrategy` for any `TenacityPropertyKey`

0.1.4
-----
* `TenacityObservableCommand` leverages the semaphore execution isolation strategy. Breakerbox now has support for configuring this through the web interface.
* Tenacity 0.5.3 - This brings ability to configure Semaphore configurations (`SemaphoreConfiguration`).
* Adjusting timeout on authentication requests to enhance the user experience (5s to 30s)
* Updating `breakerbox-dashboard` to include support for semaphore rejection and better error messages when problems occur
* Removing a bug where breakerbox would load two different versions, it now only loads one

0.1.3
-----
* Tenacity 0.4.5
* H2 1.4.186
* Postgresql 9.4-1201-jdbc41

0.1.2
-----
* Tenacity 0.4.4
* H2 1.4.185
* Adding new configuration parameter to define the default dashboard.

0.1.1
--------------
* Builds against Java8 now
* Dropwizard 0.7.1
* Tenacity 0.3.6
* Turbine 1.0.0
* Changing default refresh rate from 2s to 1s
* Moving sync refresh rate from 5s to 30s (but may need to be based on number of hosts)
* Moving sync state block to bottom of page until resizing problem is fixed

0.1.0
-----
* Initial release
