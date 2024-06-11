package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.wfparser.SimulationScheduler;

import java.io.IOException;

public class SimulationLifeCycleDeserializer extends JsonDeserializer<SimulationScheduler> {

    @Override
    public SimulationScheduler deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        long meanRequestInterval = node.get("meanRequestInterval").asLong();
        long totalSimulationTime = node.get("totalSimulationTime").asLong();
        long sampleInterval = node.get("sampleInterval").asLong();

        return new SimulationScheduler(meanRequestInterval, totalSimulationTime, sampleInterval);
    }
}
