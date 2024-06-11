package org.wfparser.workflowparser;

import java.io.IOException;

public interface WfCommonsWorkflowDeserializer {

    WorkflowInstance deserialize(String jsonFileInput) throws IOException;
}
