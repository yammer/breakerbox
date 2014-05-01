Service
-------
1. Upgrade to Dropwizard 0.7.0
2. Stop using an iframe for the Hystrix-dashboard, we can serve it up directly.
3. Frontend adjustments for the configure page. Saving and refreshing is quite jarring when you have a large list of hosts.
4. Latest turbine (0.4 is way out of date).

Jdbi
-----
Currently supports H2/Postgres out of the box.

1. Officially add MySQL support.

Azure
-----
1. Upgrade Azure client to latest.
2. Generate a decent Mock for Azure Table. For now they are all @Ignored if you don't have valid Azure Table credentials.
