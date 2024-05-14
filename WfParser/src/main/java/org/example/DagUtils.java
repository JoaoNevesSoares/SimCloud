package org.example;

import org.example.WorkflowParser.CloudletWorkflow;
import org.example.WorkflowParser.Workflow;
import org.example.WorkflowParser.WorkflowTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
