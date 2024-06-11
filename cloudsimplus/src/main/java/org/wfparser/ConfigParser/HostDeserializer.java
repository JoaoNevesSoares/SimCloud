package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HostDeserializer extends JsonDeserializer<HostSimple> {

    @Override
    public HostSimple deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        long ram = node.get("ram").asLong();
        long bw = node.get("bw").asLong();
        long storage = node.get("storage").asLong();

        List<Pe> pes = new ArrayList<>();
        JsonNode pesNode = node.get("pes");
        if(pesNode != null) {
            ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
            PesSimpleDeserializer pesSimpleDeserializer = new PesSimpleDeserializer();
            for(JsonNode peNode : pesNode) {
                pes.add(pesSimpleDeserializer.deserialize(peNode.traverse(mapper),deserializationContext));
            }
        }
        VmSchedulerTimeShared vmScheduler = null;
        JsonNode scheduler = node.get("vmScheduler");
        if(scheduler != null && scheduler.asText().equals("vmSchedulerTimeShared")) {
            vmScheduler = new VmSchedulerTimeShared();
        }
        HostSimple host = new HostSimple(ram,bw,storage,pes);
        host.setVmScheduler(vmScheduler);
        return host;
    }
}
