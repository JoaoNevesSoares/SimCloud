package org.cloudsimplus;

import java.util.List;

public class Workflow {

    private int makespan;
    private List<Activity> tasks;
    public Workflow() {
    }

    public int getMakespan() {
        return makespan;
    }

    public void setMakespan(int makespan) {
        this.makespan = makespan;
    }

    public List<Activity> getTasks() {
        return tasks;
    }

    public void setTasks(List<Activity> tasks) {
        this.tasks = tasks;
    }
}
