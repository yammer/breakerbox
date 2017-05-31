package com.yammer.breakerbox.turbine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.breakerbox.turbine.client.MarathonClient;
import com.yammer.breakerbox.turbine.config.MarathonClientConfiguration;
import com.yammer.breakerbox.turbine.model.marathon.MarathonClientResponse;
import com.yammer.breakerbox.turbine.model.marathon.PortMapping;
import com.yammer.breakerbox.turbine.model.marathon.Task;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by supreeth.vp on 23/05/17.
 */
public class MarathonInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonInstanceDiscovery.class);
    private final ObjectMapper mapper;
    private  MarathonClient marathonClient;
    private final List<MarathonClientConfiguration> marathonClientConfigurations;
    private Map<MarathonClientConfiguration,Invocation.Builder> marathonClientConfigurationBuilderMap;

    public MarathonInstanceDiscovery(ObjectMapper mapper, List<MarathonClientConfiguration> marathonClientConfigurations) {
        this.mapper = mapper;
        this.marathonClientConfigurations = marathonClientConfigurations;
         constructMarathonClientConfigurationBuilderMap();
    }

    private void constructMarathonClientConfigurationBuilderMap() {
        marathonClientConfigurationBuilderMap = new HashMap<>();
        marathonClientConfigurations.parallelStream().forEach(marathonClientConfiguration -> {
            marathonClient = new MarathonClient(marathonClientConfiguration);
            Invocation.Builder builder = marathonClient.getServiceInstanceDetails();
            marathonClientConfigurationBuilderMap.put(marathonClientConfiguration,builder);
        });

    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        List<Instance> instances = new ArrayList<>();
        marathonClientConfigurationBuilderMap.entrySet().parallelStream().forEach(entry -> {
            Response response = null;
            try {
                response = entry.getValue().get();
                if (response.getStatus() == HttpStatus.SC_OK) {
                   instances.addAll(createServiceInstanceList(response.readEntity(String.class), entry.getKey()));
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
        return instances;
    }

     public List<Instance> createServiceInstanceList(String marathonApiResponse,MarathonClientConfiguration marathonClientConfiguration) {
         MarathonClientResponse marathonClientResponse = null;
         try {
             marathonClientResponse = mapper.readValue(marathonApiResponse, MarathonClientResponse.class);
         } catch (IOException e) {
             LOGGER.error("io exception",e);
         }
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
            return tasks.stream().map(task -> new Instance(task.getHost() + ":" + task.getPorts().get(finalPortIndex), marathonClientConfiguration.getCluster(), true)).collect(Collectors.toList());
        } else {
            LOGGER.error("tasks not available for the given namespace");
            return Collections.emptyList();
        }
    }
}
