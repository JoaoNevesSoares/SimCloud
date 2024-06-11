package org.wfparser.workflowparser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(value = {"type","command","id","category",})
@Data
public class WorkflowTask {

    private String name;
    private List<String> parents;
    private List<String> children;
    private long runtimeInSeconds;
    private short cores;
    private String startedAt;
    private List<TaskFile> files;
}
