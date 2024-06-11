package org.wfparser.ConfigParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.vms.VmSimple;
import org.wfparser.DagUtils;
import org.wfparser.SimulationScheduler;
import org.wfparser.workflowparser.CloudletWorkflow;
import org.wfparser.workflowparser.WfCommonsWorkflowDeserializerImpl;
import org.wfparser.workflowparser.WorkflowInstance;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PlatformUtils {
    private static CloudSimPlus simulation;

    private PlatformUtils(){}

    public static void init(CloudSimPlus sim) {
        PlatformUtils.simulation = sim;
    }
    public static List<DatacenterSimple> loadPlatform(String fileName) {
        if (simulation == null) {
            throw new IllegalStateException("CloudSimPlus has not been initialized. Call PlatformUtils.init() first.");
        }
        var mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DatacenterSimple.class, new DatacenterDeserializer(simulation));
        mapper.registerModule(module);
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, DatacenterSimple.class);
        try (InputStream inputStream = new FileInputStream(fileName)) {
            return mapper.readValue(inputStream, listType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static VmSimple loadVmTemplate(String fileName) {
        var mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(VmSimple.class, new VmTemplateDeserializer());
        mapper.registerModule(module);
        try (InputStream inputStream = new FileInputStream(fileName)) {
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode vmNode = rootNode.get("vm");
            if (vmNode == null) {
                throw new IllegalArgumentException("The YAML file does not contain 'vm' key at the expected location.");
            }
            return mapper.treeToValue(vmNode, VmSimple.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static SimulationScheduler loadSimulation(String fileName) {
        var mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimulationScheduler.class, new SimulationLifeCycleDeserializer());
        mapper.registerModule(module);
        try (InputStream inputStream = new FileInputStream(fileName)) {
            return mapper.readValue(inputStream, SimulationScheduler.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<CloudletWorkflow> loadWorkflow(String fileName) {
        final var workflowDeserializer = new WfCommonsWorkflowDeserializerImpl(new ObjectMapper());
        WorkflowInstance workflow = workflowDeserializer.deserialize(fileName);
        return DagUtils.createDagFromWorkflow(workflow.getWorkflow());
    }
}
