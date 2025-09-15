package com.kariimhosny.filesyncserver.auth.services.impl;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kariimhosny.filesyncserver.auth.dto.AuthResponse;
import com.kariimhosny.filesyncserver.auth.dto.LoginRequest;
import com.kariimhosny.filesyncserver.auth.dto.RegisterRequest;
import com.kariimhosny.filesyncserver.auth.enrtities.User;
import com.kariimhosny.filesyncserver.auth.repositories.contracts.IUserRepository;
import com.kariimhosny.filesyncserver.auth.services.contracts.IJWTServices;
import com.kariimhosny.filesyncserver.auth.services.contracts.IUserServices;
import com.kariimhosny.filesyncserver.common.dto.ApiResponse;

@Service
public class UserServices implements IUserServices {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJWTServices jwtServices;

    public UserServices(IUserRepository userRepository, PasswordEncoder passwordEncoder, IJWTServices jwtServices) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtServices = jwtServices;
    }

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .build();
                    
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email already in use"));
        }

        try {
            // Create new user
            User user = new User(
                    request.getName(),
                    request.getEmail(),
                    passwordEncoder.encode(request.getPassword())
            );

            // Save user
            userRepository.save(user);

            // Generate JWT token
            String token = jwtServices.issueToken(user);

            // Create auth response
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .build();
                    
            // Return success response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(authResponse, "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(LoginRequest request) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            // Check if user exists
            if (userOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid email or password"));
            }
            
            User user = userOptional.get();
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid email or password"));
            }
            
            // Generate JWT token
            String token = jwtServices.issueToken(user);
            
            // Create auth response
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .build();
            
            // Return success response
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
}