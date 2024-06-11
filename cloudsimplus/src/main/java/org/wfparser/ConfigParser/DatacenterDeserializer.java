package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterCharacteristicsSimple;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.HostSimple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatacenterDeserializer extends JsonDeserializer<DatacenterSimple> {
    private final CloudSimPlus simulation;

    public DatacenterDeserializer(CloudSimPlus simulation) {
        this.simulation = simulation;
    }
    @Override
    public DatacenterSimple deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        List<HostSimple> hosts = new ArrayList<>();
        JsonNode hostsNode = node.get("hosts");
        if (hostsNode != null) {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            HostDeserializer hostDeserializer = new HostDeserializer();
            for (JsonNode hostNode : hostsNode) {
                hosts.add(hostDeserializer.deserialize(hostNode.traverse(objectMapper), ctxt));
            }
        }
        DatacenterSimple datacenter = new DatacenterSimple(this.simulation, hosts);

        DatacenterCharacteristicsSimple datacenterCharacteristicsSimple = null;
        JsonNode datacenterCharacteristicsNode = node.get("datacenterCharacteristics");
        if (datacenterCharacteristicsNode != null) {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            DatacenterCharacteristicsDeserializer characteristicsDeserializer = new DatacenterCharacteristicsDeserializer();
            datacenterCharacteristicsSimple = characteristicsDeserializer.deserialize(datacenterCharacteristicsNode.traverse(objectMapper), ctxt);
        }
        datacenter.setCharacteristics(datacenterCharacteristicsSimple);

        VmAllocationPolicy vmAllocationPolicy = null;
        JsonNode vmAllocationPolicyNode = node.get("vmAllocationPolicy");
        if(vmAllocationPolicyNode != null && vmAllocationPolicyNode.asText().equals("vmAllocationPolicyRoundRobin")) {
            vmAllocationPolicy = new VmAllocationPolicyRoundRobin();
        }
        datacenter.setVmAllocationPolicy(vmAllocationPolicy);

        String id = node.get("id").asText();
        datacenter.setName(id);
        double schedulingInterval = node.get("schedulingInterval").asDouble();
        datacenter.setSchedulingInterval(schedulingInterval);
        return datacenter;
    }
}
