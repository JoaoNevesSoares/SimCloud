package org.example.WorkflowParser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Workflow {

    private String executedAt;
    private int makespanInSeconds;
    private List<WorkflowTask> tasks = new ArrayList<>();
}
