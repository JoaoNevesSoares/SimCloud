package org.wfparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wfparser.workflowparser.*;

import java.util.ArrayList;
import java.util.List;

// Utility class for DAG creation from workflow description
public class DagUtils {

    public static List<CloudletWorkflow> createDagFromWorkflow(Workflow deserializedWorkflow) {
        List<CloudletWorkflow> dag = new ArrayList<>();
        for (WorkflowTask task : deserializedWorkflow.getTasks()) {
            dag.add(createCloudletFromWorkflowTasks(task));
        }
        solveDependencies(dag,deserializedWorkflow.getTasks());
        return dag;
    }
    public static List<CloudletWorkflow> loadWorkflow(String workflowFilePath) {
        final var workflowDeserializer = new WfCommonsWorkflowDeserializerImpl(new ObjectMapper());
        WorkflowInstance workflow = workflowDeserializer.deserialize(workflowFilePath);
        return createDagFromWorkflow(workflow.getWorkflow());
    }
    private static void solveDependencies(List<CloudletWorkflow> dag, List<WorkflowTask> deserialized) {
        for (WorkflowTask task : deserialized) {
            CloudletWorkflow cloudletWorkflow = findRealTask(task.getName(),dag);
            for(String childrenName : task.getChildren()) {
                CloudletWorkflow childCloudletWorkflow = findRealTask(childrenName,dag);
                assert cloudletWorkflow != null;
                assert childCloudletWorkflow != null;
                cloudletWorkflow.addSuccessorTask(childCloudletWorkflow);
            }
            for(String parentName : task.getParents()) {
                CloudletWorkflow parentCloudletWorkflow = findRealTask(parentName,dag);
                assert cloudletWorkflow != null;
                assert parentCloudletWorkflow != null;
                cloudletWorkflow.addPriorTask(parentCloudletWorkflow);
            }
        }
    }
    private static CloudletWorkflow findRealTask(String Name, List<CloudletWorkflow> dag) {
        for(CloudletWorkflow task : dag){
            if(task.getName().equals(Name)){
                return task;
            }
        }
        return null;
    }
    private static CloudletWorkflow createCloudletFromWorkflowTasks(WorkflowTask task) {
        return new CloudletWorkflow(task.getRuntimeInSeconds(), task.getName());
    }
}
