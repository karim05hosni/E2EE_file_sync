package com.karimhosny.auth.entities;

public class User {
    private Long id;

    private String name;
    
    private String email;

    private String jwtToken;
    
    // private String password;  // This will store the hashed password
    
    private Long spaceId;  // Foreign key to spaces table

    public User(){
        
    }
    public User(Long id, String name, String email /*,String password*/, Long spaceId, String jwtToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        // this.password = password;
        this.spaceId = spaceId;
        this.jwtToken = jwtToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public Long getSpaceId() {
        return spaceId;
    }
    
    
}
