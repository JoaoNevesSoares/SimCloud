package org.wfparser.ConfigParser;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.vms.VmSimple;

import java.io.IOException;

public class VmTemplateDeserializer  extends JsonDeserializer<VmSimple> {

    @Override
    public VmSimple deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        long mipsCapacity = node.get("mipsCapacity").asLong();
        long pesNumber = node.get("pesNumber").asLong();
        long ram = node.get("ram").asLong();
        long bw = node.get("bw").asLong();
        long storage = node.get("storage").asLong();
        CloudletScheduler cloudletScheduler = null;
        JsonNode cloudletSchedulerNode = node.get("cloudletScheduler");
        if (cloudletSchedulerNode != null && cloudletSchedulerNode.asText().equals("CloudletSchedulerTimeShared")) {
            cloudletScheduler = new CloudletSchedulerTimeShared();
        }
        VmSimple vm = new VmSimple(mipsCapacity,pesNumber);
        vm.setRam(ram)
                .setBw(bw)
                .setSize(storage)
                .setCloudletScheduler(cloudletScheduler);
        return vm;
    }
}
