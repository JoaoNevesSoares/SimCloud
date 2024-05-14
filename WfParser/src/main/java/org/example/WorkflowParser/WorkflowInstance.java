package org.example.WorkflowParser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(value = {"author","wms"})
@Data
public class WorkflowInstance {

    private String name;
    private String description;
    private String createdAt;
    private String schemaVersion;
    private Workflow workflow;
}
