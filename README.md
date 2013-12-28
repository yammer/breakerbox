Breakerbox
==========

A dashboard of [Tenacity](https://github.com/yammer/tenacity) circuit-breakers and threadpools displayed in real-time and the ability to configure them on-the-fly.

[Tenacity](https://github.com/yammer/tenacity) is a [Dropwizard](http://www.dropwizard.io)-module that brings together [Dropwizard](http://www.dropwizard.io)+[Hystrix](https://github.com/Netflix/Hystrix).

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
