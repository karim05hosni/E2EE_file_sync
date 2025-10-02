package com.kariimhosny.filesyncserver.sync.websocket;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.auth.services.contracts.IJWTServices;

import io.jsonwebtoken.Claims;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final IJWTServices jwtService;
    private final WebSocketSessionManager wsSessionManager;


    /**
     *
     * @param jwtService
     */
    public AuthHandshakeInterceptor(WebSocketSessionManager wsSessionManager, IJWTServices jwtService) {
        this.jwtService = jwtService;
        this.wsSessionManager = wsSessionManager;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String token = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {

            token = servletRequest.getServletRequest().getHeader("Authorization");
            System.out.println("Token: " + token);
            System.out.println("isValid: " + jwtService.isValidToken(token));

        }

        if (token != null && jwtService.isValidToken(token)) {
            System.out.println("From websocket: Valid token");
            Claims claims = jwtService.extractClaims(token);
            System.out.println("Token Claims" + claims);
            String username = claims.get("username", String.class);
            Long user_id = claims.get("userId", Long.class);
            Long space_id = claims.get("spaceId", Long.class);

            AuthUser user = new AuthUser(user_id, username, space_id);

            Authentication auth;
            auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null, // no credentials
                    List.of() // no authorities (add if you need role-based auth)
            );
            attributes.put("auth", user);
            
            System.out.println("Thread name from authintereceptor.beforehandshake: " + Thread.currentThread().getName());
            return true; // allow handshake
        }


        response.setStatusCode(HttpStatus.FORBIDDEN);
        return false; // block handshake

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            System.out.println("Thread name from authintereceptor.afterhandshake: " + Thread.currentThread().getName());
        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
