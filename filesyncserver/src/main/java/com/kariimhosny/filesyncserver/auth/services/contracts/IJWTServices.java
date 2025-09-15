package com.kariimhosny.filesyncserver.auth.services.contracts;

import com.kariimhosny.filesyncserver.auth.enrtities.User;

import io.jsonwebtoken.Claims;

public interface IJWTServices {
    /**
     * Issue a JWT token for a specific user
     * 
     * @param user The user to issue a token for
     * @return The JWT token string
     */
    String issueToken(User user);

    boolean isValidToken(String token);

    Claims extractClaims(String token);

    String extractUserId(String token);

    boolean isTokenExpired(String token);
}
