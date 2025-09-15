package com.kariimhosny.filesyncserver.auth.api;

import org.springframework.stereotype.Component;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthUser {
    private  Long id;
    private  String name;

    public AuthUser(Long id, String name){
        this.id = id;
        this.name = name;
    }
    public Long getId() { return id; }
    public String getUsername() { return name; }

}
