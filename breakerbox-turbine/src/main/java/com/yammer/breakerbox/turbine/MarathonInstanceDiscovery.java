package com.yammer.breakerbox.turbine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.breakerbox.turbine.client.MarathonClient;
import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;
import com.yammer.breakerbox.turbine.model.MarathonClientResponse;
import com.yammer.breakerbox.turbine.model.PortMapping;
import com.yammer.breakerbox.turbine.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonInstanceDiscovery.class);
    private final MarathonClient marathonClient;
    private final ObjectMapper mapper;
    private final MarathonClientConfiguration marathonClientConfiguration;

    public MarathonInstanceDiscovery(MarathonClientConfiguration marathonClientConfiguration, ObjectMapper mapper) {

        this(new MarathonClient(marathonClientConfiguration), mapper, marathonClientConfiguration);
    }

    public MarathonInstanceDiscovery(MarathonClient marathonClient, ObjectMapper mapper, MarathonClientConfiguration marathonClientConfiguration) {
        this.marathonClient = marathonClient;
        this.mapper = mapper;
        this.marathonClientConfiguration = marathonClientConfiguration;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        Response response = marathonClient.getServiceInstanceDetails();
        return response.getStatus() == 200
                ? createServiceInstanceList(response.readEntity(String.class))
                : Collections.emptyList();
    }

    private Collection<Instance> createServiceInstanceList(String marathonApiResponse) throws IOException {
        MarathonClientResponse marathonClientResponse = mapper.readValue(marathonApiResponse, MarathonClientResponse.class);
        if (marathonClientResponse != null && marathonClientResponse.getApp() != null) {
            List<PortMapping> portMappingList = marathonClientResponse.getApp().getContainer().getDocker().getPortMappings();
            int portIndex = -1;
            for (int i = 0; i < portMappingList.size(); i++) {
                if (portMappingList.get(i).getContainerPort().equals(marathonClientConfiguration.getMarathonAppPort())) {
                    portIndex = i;
                    break;
                }
            }
            if (portIndex < 0) {
                LOGGER.error("marathon app port non present in port mapping");
                return Collections.emptyList();
            }

            List<Task> tasks = marathonClientResponse.getApp().getTasks();


            int finalPortIndex = portIndex;
            List<Instance> instances = tasks.stream()
                    .map(task -> new Instance(task.getHost() + ":" + task.getPorts().get(finalPortIndex), marathonClientConfiguration.getMarathonAppNameSpace(), true)).collect(Collectors.toList());
            return instances;
        } else {
            LOGGER.error("tasks not available for the given namespace");
            return Collections.emptyList();
        }
    }
}
