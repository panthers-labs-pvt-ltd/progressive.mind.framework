package com.progressive.minds.chimera.core.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DataIngestionWorkflow {
    
    @WorkflowMethod
    void runPipeline(String pipelineId);
}

