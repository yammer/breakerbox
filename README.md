Breakerbox
========

What is it?
-----------
A dashboard of Tenacity circuit-breakers displayed in real-time and the ability to configure these circuit-breakers on-the-fly.

What is Tenacity?
-----------------
https://github.int.yammer.com/yammer/tenacity

How do I add a service to Breakerbox?
-------------------------------------
You'll want to clone the `https://github.int.yammer.com/yammer/breakerbox-configuration.git` repository and then edit
https://github.int.yammer.com/yammer/breakerbox-configuration/blob/master/modules/breakerbox/files/config.properties.
You'll want to do the two following edits:

1. Add a new `turbine.ConfigPropertyBasedDiscovery.{serviceName}.instances` where `{serviceName}` is replaced with `completie-prod` or `completie-staging` for example. Please don't forget to include
the port at which your service is running (e.g. 8080).

2. If your service addition is in the production environment, consider adding it to the meta-dashboard `turbine.ConfigPropertyBasedDiscovery.production.instances`.

Then commit, push, repackage, and redeploy the latest version of `breakerbox`!
