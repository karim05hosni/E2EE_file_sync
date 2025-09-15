package com.kariimhosny.filesyncserver.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kariimhosny.filesyncserver.auth.dto.AuthResponse;
import com.kariimhosny.filesyncserver.auth.dto.LoginRequest;
import com.kariimhosny.filesyncserver.auth.dto.RegisterRequest;
import com.kariimhosny.filesyncserver.auth.services.contracts.IUserServices;
import com.kariimhosny.filesyncserver.common.dto.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth")
public class authController {

    private final IUserServices userService;

    public authController(IUserServices userService){
        this.userService = userService;
    }

    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

}
