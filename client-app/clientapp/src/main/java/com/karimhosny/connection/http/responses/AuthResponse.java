package com.karimhosny.connection.http.responses;


public class AuthResponse {
    private Long id;
    private String token;
    private String name;
    private Long spaceId;

    // getter + setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
    
}
