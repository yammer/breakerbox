package com.yammer.breakerbox.turbine;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.yammer.breakerbox.turbine.client.RancherClient;
import com.yammer.breakerbox.turbine.config.RancherInstanceConfiguration;

public class RancherInstanceDiscovery implements InstanceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(RancherInstanceDiscovery.class);
    private RancherClient rancherClient;
    private ObjectMapper mapper;

    public RancherInstanceDiscovery(RancherInstanceConfiguration instanceConfiguration,
                                    ObjectMapper mapper) {
        this.rancherClient = new RancherClient(instanceConfiguration);
        this.mapper = mapper;
    }

    @Override
    public Collection<Instance> getInstanceList() throws Exception {
        Response response = rancherClient.getServiceInstanceDetails();
        return response.getStatus() == 200
                ? createServiceInstanceList(response.readEntity(String.class))
                : Collections.emptyList();
    }

    private Collection<Instance> createServiceInstanceList(String rancherServiceApiResponse) throws IOException {
        JsonNode serviceJsonResponseNode = mapper.readValue(rancherServiceApiResponse, JsonNode.class);
        List<JsonNode> dataList = convertJsonArrayToList((ArrayNode) serviceJsonResponseNode.get("data"));
        Collection<Instance> instances =  dataList.stream()
                .filter(this::isServiceDashboardEnabled)
                .map(this::createInstanceList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        instances.addAll(createProductionDashboard(instances));
        return instances;
    }

    private Collection<? extends Instance> createProductionDashboard(Collection<Instance> instances) {
        return instances.stream()
                .map(instance -> new Instance(instance.getHostname(), "production", true))
                .collect(Collectors.toList());
    }

    private boolean isServiceDashboardEnabled(JsonNode dataNode) {
        JsonNode serviceStatus = getRancherLabels(dataNode).get("tenacity.metrics.stream.enabled");
        return !Objects.isNull(serviceStatus) ? serviceStatus.asBoolean() : Boolean.FALSE;
    }

    private List<Instance> createInstanceList(JsonNode dataNode) {
        String clusterName = getServiceClusterName(dataNode);
        int metricsStreamPort = getServiceMetricsStreamPort(dataNode);
        List<JsonNode> publicEndpoints = getPublicEndpoints(dataNode);
        return publicEndpoints.stream()
                .filter(jsonNodes -> jsonNodes.get("port").asInt() == metricsStreamPort)
                .map(jsonNode -> createTurbineInstance(jsonNode, clusterName))
                .collect(Collectors.toList());
    }

    private String getServiceClusterName(JsonNode dataNode) {
        JsonNode clusterNameLabel = getRancherLabels(dataNode).get("service.cluster.name");
        return !Objects.isNull(clusterNameLabel) ? clusterNameLabel.asText() : dataNode.get("name").asText();
    }

    private int getServiceMetricsStreamPort(JsonNode dataNode) {
        JsonNode portLabel = getRancherLabels(dataNode).get("tenacity.metrics.stream.port");
        return !Objects.isNull(portLabel) ? portLabel.asInt() : 8080;
    }

    private List<JsonNode> getPublicEndpoints(JsonNode objectNode) {
        return objectNode.get("publicEndpoints").getNodeType().equals(JsonNodeType.ARRAY)
                ? convertJsonArrayToList((ArrayNode) objectNode.get("publicEndpoints"))
                : Collections.emptyList();
    }

    private Instance createTurbineInstance(JsonNode jsonNode, String clusterName) {
        String hostAndPort = jsonNode.get("ipAddress").asText() + ":" + jsonNode.get("port").asText();
        return new Instance(hostAndPort, clusterName, true);
    }

    private JsonNode getRancherLabels(JsonNode dataNode) {
        return dataNode.get("launchConfig").get("labels");
    }

    private List<JsonNode> convertJsonArrayToList(ArrayNode arrayNode) {
        try {
            return mapper.readValue(arrayNode.toString(), TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, JsonNode.class));
        } catch (IOException e) {
            LOGGER.error("Failed to convert ArrayNode to List<JsonNode>", e);
            return Collections.emptyList();
        }
    }
}
