package org.example.WorkflowParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WfCommonsWorkflowDeserializerImpl implements WfCommonsWorkflowDeserializer {

    private final ObjectMapper objectMapper;

    public WfCommonsWorkflowDeserializerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowInstance deserialize(String jsonFileInput) {
        try (InputStream inputStream = new FileInputStream(jsonFileInput)) {
            return objectMapper.readValue(inputStream, WorkflowInstance.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
