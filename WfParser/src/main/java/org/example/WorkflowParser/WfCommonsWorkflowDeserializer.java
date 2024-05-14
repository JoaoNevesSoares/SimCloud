package org.example.WorkflowParser;

import java.io.IOException;

public interface WfCommonsWorkflowDeserializer {

    WorkflowInstance deserialize(String jsonFileInput) throws IOException;
}
