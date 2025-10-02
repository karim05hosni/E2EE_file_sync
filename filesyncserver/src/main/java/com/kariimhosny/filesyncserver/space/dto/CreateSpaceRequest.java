package com.kariimhosny.filesyncserver.space.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSpaceRequest {
    @NotBlank(message = "Space name is required")
    @Size(max = 255, message = "Space name too long")
    private String name;
}
