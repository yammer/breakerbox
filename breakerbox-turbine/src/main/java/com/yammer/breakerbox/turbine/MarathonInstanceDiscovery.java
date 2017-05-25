package com.yammer.breakerbox.turbine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.breakerbox.turbine.client.MarathonClient;
import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;
import com.yammer.breakerbox.turbine.model.MarathonClientResponse;
import com.yammer.breakerbox.turbine.model.PortMapping;
import com.yammer.breakerbox.turbine.model.Task;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonInstanceDiscovery.class);
    private final ObjectMapper mapper;
    private  MarathonClient marathonClient;
    private final List<MarathonClientConfiguration> marathonClientConfigurations;



    public MarathonInstanceDiscovery(ObjectMapper mapper, List<MarathonClientConfiguration> marathonClientConfigurations) {
        this.mapper = mapper;
        this.marathonClientConfigurations = marathonClientConfigurations;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        List<Instance> instances = new ArrayList<>();
        marathonClientConfigurations.parallelStream().forEach(marathonClientConfiguration -> {
            marathonClient = new MarathonClient(marathonClientConfiguration);
            Response response = marathonClient.getServiceInstanceDetails();
            if(response.getStatus() == HttpStatus.SC_OK){
                try {
                    instances.addAll(createServiceInstanceList(response.readEntity(String.class),marathonClientConfiguration));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return instances;
    }

     public List<Instance> createServiceInstanceList(String marathonApiResponse,MarathonClientConfiguration marathonClientConfiguration) throws IOException {
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
                    .map(task -> new Instance(task.getHost() + ":" + task.getPorts().get(finalPortIndex), marathonClientConfiguration.getCluster(), true)).collect(Collectors.toList());
            return instances;
        } else {
            LOGGER.error("tasks not available for the given namespace");
            return new ArrayList<>();
        }
    }
}
