package com.kariimhosny.filesyncserver.space.controllers;

import com.kariimhosny.filesyncserver.space.dto.CreateSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.JoinSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.SpaceResponse;
import com.kariimhosny.filesyncserver.common.dto.ApiResponse;
import com.kariimhosny.filesyncserver.space.services.contracts.ISpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/space")
public class SpaceController {
    private final ISpaceService spaceService;

    @Autowired
    public SpaceController(ISpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SpaceResponse>> createSpace(@RequestBody CreateSpaceRequest request) {
        SpaceResponse response = spaceService.createSpace(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Space created successfully"));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<SpaceResponse>> joinSpace(@RequestBody JoinSpaceRequest request) {
        SpaceResponse response = spaceService.joinSpace(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Joined space successfully"));
    }
}
