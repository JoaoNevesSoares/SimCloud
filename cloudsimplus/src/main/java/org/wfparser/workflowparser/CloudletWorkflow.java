package org.wfparser.workflowparser;

import org.cloudsimplus.cloudlets.CloudletSimple;

import java.util.ArrayList;
import java.util.List;


public class CloudletWorkflow extends CloudletSimple {

    private static final int PES = 1;
    private final String name;
    private final List<CloudletWorkflow> priorTasks;
    private final List<CloudletWorkflow> successorTasks;

    public CloudletWorkflow(long runtimeInSeconds, String name) {
        super( runtimeInSeconds, PES);
        this.name = name;
        this.priorTasks = new ArrayList<>();
        this.successorTasks = new ArrayList<>();
    }

    public void addPriorTask(CloudletWorkflow workflow) {
        this.priorTasks.add(workflow);
    }
    public void addSuccessorTask(CloudletWorkflow workflow) {
        this.successorTasks.add(workflow);
    }
    public boolean isDependenciesCompleted() {
        boolean completed = true;
        for (CloudletWorkflow priorTask: this.priorTasks) {
            if(!priorTask.isFinished()) {
                completed = false;
                break;
            }
        }
        return completed;
    }
    public String getName() {
        return name;
    }

    public List<CloudletWorkflow> getPriorTasks() {
        return priorTasks;
    }

    public List<CloudletWorkflow> getSuccessorTasks() {
        return successorTasks;
    }
}
