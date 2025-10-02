package com.karimhosny.space.services.contracts;

import java.io.IOException;

import com.karimhosny.connection.http.responses.BaseResponse;
import com.karimhosny.space.dto.CreateSpaceRequest;
import com.karimhosny.space.dto.JoinSpaceRequest;
import com.karimhosny.space.dto.SpaceResponse;

public interface ISpaceService {
    BaseResponse<SpaceResponse> createSpace(CreateSpaceRequest request, String token) throws IOException, InterruptedException;
    BaseResponse<SpaceResponse> joinSpace(JoinSpaceRequest request, String token) throws IOException, InterruptedException;
}
