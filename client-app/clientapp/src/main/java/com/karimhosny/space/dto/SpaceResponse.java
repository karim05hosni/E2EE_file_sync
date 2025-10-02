package com.karimhosny.space.dto;

public class SpaceResponse {
    private Long id;
    private String name;
    private Long owner;
    private String ownerName;
    private Integer memberCount;

    public SpaceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOwner() { return owner; }
    public void setOwner(Long owner) { this.owner = owner; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
}
