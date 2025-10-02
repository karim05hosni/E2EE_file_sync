package com.kariimhosny.filesyncserver.space.services.contracts;

import com.kariimhosny.filesyncserver.space.dto.CreateSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.JoinSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.SpaceResponse;

public interface ISpaceService {
    SpaceResponse joinSpace(JoinSpaceRequest request);
    SpaceResponse createSpace(CreateSpaceRequest request);
}
