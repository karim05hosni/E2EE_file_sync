package com.kariimhosny.filesyncserver.space.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class JoinSpaceRequest {
    @NotNull(message = "Space ID is required")
    private Long spaceId;
}