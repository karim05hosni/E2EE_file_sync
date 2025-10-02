package com.karimhosny.space.dto;


public class JoinSpaceRequest {
    private Long spaceId;

    public JoinSpaceRequest() {}
    public JoinSpaceRequest(Long spaceId) { this.spaceId = spaceId; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
}
