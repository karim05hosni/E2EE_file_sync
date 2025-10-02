package com.kariimhosny.filesyncserver.space.dto;

import lombok.Data;

@Data
public class SpaceResponse {
    private Long id;
    private String name;
    private Long owner;
    private String ownerName;
    private Integer memberCount;
}

