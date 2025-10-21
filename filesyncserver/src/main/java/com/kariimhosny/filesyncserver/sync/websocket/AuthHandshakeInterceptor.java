package com.kariimhosny.filesyncserver.sync.websocket;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.kariimhosny.filesyncserver.auth.services.contracts.IJWTServices;

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
            
            attributes.put("id", jwtService.extractUserId(token));
            attributes.put("spaceId", jwtService.extractClaims(token).get("spaceId"));
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
