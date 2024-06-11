package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cloudsimplus.datacenters.DatacenterCharacteristics;
import org.cloudsimplus.datacenters.DatacenterCharacteristicsSimple;

import java.io.IOException;

public class DatacenterCharacteristicsDeserializer extends JsonDeserializer<DatacenterCharacteristicsSimple> {

    @Override
    public DatacenterCharacteristicsSimple deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        double cpuCost = node.get("cpuCost").asDouble();
        double ramCost = node.get("ramCost").asDouble();
        double storageCost = node.get("storageCost").asDouble();
        DatacenterCharacteristics.Distribution distribution = DatacenterCharacteristics.Distribution.valueOf(node.get("distribution").asText().toUpperCase());
        return new DatacenterCharacteristicsSimple(cpuCost, ramCost, storageCost).setDistribution(distribution);
    }
}
