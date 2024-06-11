package org.wfparser.tablebuilders.historyentries;

public record BrokerHistoryEntry(String brokerName,
                                 String workflowName,
                                 long submittedTasks,
                                 long completedTasks,
                                 long failedTasks,
                                 double startTime,
                                 double finishTime,
                                 double makespan,
                                 double averageTaskMakespan
                                 ) {
    public String brokerName() {
        return brokerName;
    }
    public String workflowName() {
        return workflowName;
    }
    public long submittedTasks() {
        return submittedTasks;
    }
    public long completedTasks() {
        return completedTasks;
    }
    public long failedTasks() {
        return failedTasks;
    }
    public double startTime() {
        return startTime;
    }
    public double finishTime() {
        return finishTime;
    }
    public double makespan() {
        return makespan;
    }
    public double averageTaskMakespan() {
        return averageTaskMakespan;
    }
}
