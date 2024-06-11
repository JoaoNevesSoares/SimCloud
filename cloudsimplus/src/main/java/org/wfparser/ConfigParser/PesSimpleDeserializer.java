package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cloudsimplus.resources.PeSimple;

import java.io.IOException;

public class PesSimpleDeserializer extends JsonDeserializer<PeSimple> {

    @Override
    public PeSimple deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        int id = (node.has("id")) ? node.get("id").asInt() : -1;
        double mipsCapacity = (node.has("mipsCapacity")) ? node.get("mipsCapacity").asDouble() : PeSimple.getDefaultMips();
        PeSimple peSimple = new PeSimple(mipsCapacity);
        peSimple.setId(id);
        return peSimple;
    }
}

