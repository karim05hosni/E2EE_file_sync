package com.kariimhosny.filesyncserver.auth.services.contracts;

import org.springframework.http.ResponseEntity;

import com.kariimhosny.filesyncserver.auth.dto.AuthResponse;
import com.kariimhosny.filesyncserver.auth.dto.LoginRequest;
import com.kariimhosny.filesyncserver.auth.dto.RegisterRequest;
import com.kariimhosny.filesyncserver.common.dto.ApiResponse;

public interface IUserServices {
    
    /**
     * Register a new user with the provided details
     * 
     * @param request The registration request containing user details
     * @return ResponseEntity with success message or error details
     */
    ResponseEntity<ApiResponse<AuthResponse>> registerUser(RegisterRequest request);
    
    /**
     * Authenticate a user with email and password
     * 
     * @param request The login request containing user credentials
     * @return ResponseEntity with JWT token or error details
     */
    ResponseEntity<ApiResponse<AuthResponse>> loginUser(LoginRequest request);
}