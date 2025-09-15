package com.kariimhosny.filesyncserver.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kariimhosny.filesyncserver.common.dto.ApiResponse;

@RestController
@RequestMapping("api/file")
public class FileController {

    @PostMapping("path")
    public ResponseEntity<ApiResponse<String>> postMethodName(Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Authentication Object: 1234 ");
        System.out.println(username);
        return ResponseEntity.ok(ApiResponse.success(username, "Username retrieved successfully"));
    }

}
