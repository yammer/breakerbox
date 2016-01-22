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
