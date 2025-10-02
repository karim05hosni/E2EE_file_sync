package com.karimhosny.space.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.connection.http.config.Client;
import com.karimhosny.space.dto.CreateSpaceRequest;
import com.karimhosny.space.dto.JoinSpaceRequest;
import com.karimhosny.space.dto.SpaceResponse;
import com.karimhosny.connection.http.responses.BaseResponse;
import com.karimhosny.space.services.contracts.ISpaceService;

import java.io.IOException;

public class SpaceService implements ISpaceService {
    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();

    public SpaceService(Client client) {
        this.client = client;
    }

    public BaseResponse<SpaceResponse> createSpace(CreateSpaceRequest request, String token) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(request);
        String respJson = client.post("api/space/create", body, token);
        return mapper.readValue(respJson, new TypeReference<BaseResponse<SpaceResponse>>(){});
    }

    public BaseResponse<SpaceResponse> joinSpace(JoinSpaceRequest request, String token) throws IOException, InterruptedException {
        String body = mapper.writeValueAsString(request);
        String respJson = client.post("api/space/join", body, token);
        return mapper.readValue(respJson, new TypeReference<BaseResponse<SpaceResponse>>(){});
    }

}
