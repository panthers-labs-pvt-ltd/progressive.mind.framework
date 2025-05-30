package com.progressive.minds.chimera.pipelineutils;

import com.progressive.minds.chimera.core.api_service.consumer.DBAPIClient;
import com.progressive.minds.chimera.core.api_service.dto.PipelineMetadata;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

public class PipelineMetadataFetcher {
    public static PipelineMetadata getPipelineMetadata(String pipelineName) throws IOException, InterruptedException {
        DBAPIClient dbClient = new DBAPIClient();
        String url = "http://localhost:8080/api/v1/pipelineMetadata/" + pipelineName;
        return dbClient.get(url, new TypeReference<PipelineMetadata>(){});
    }
}
