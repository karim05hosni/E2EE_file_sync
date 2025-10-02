package com.kariimhosny.filesyncserver.auth.api;

import org.springframework.stereotype.Component;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthUser {
    private  Long id;
    private  String name;
    private  Long SpaceId;
    public AuthUser(Long id, String name, Long SpaceId){
        this.id = id;
        this.name = name;
        this.SpaceId = SpaceId;
    }
    public Long getId() { return id; }
    public String getUsername() { return name; }
    public Long getSpaceId() {return SpaceId;}

}
