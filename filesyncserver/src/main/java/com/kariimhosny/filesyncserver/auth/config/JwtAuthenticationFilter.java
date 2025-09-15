package com.kariimhosny.filesyncserver.auth.config;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.auth.services.contracts.IJWTServices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
// @RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJWTServices jwtTokenService;

    public JwtAuthenticationFilter(IJWTServices jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {

        // get the request header
        String authHeader = request.getHeader("Authorization");

        // check if request header exists
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // parse the token from the header
            String token = authHeader.substring(7);

            // validate the token
            if (jwtTokenService.isValidToken(token)) {
                // extract the claims (token data)
                Claims claims = jwtTokenService.extractClaims(token);
                String username = claims.get("username", String.class);
                Long user_id = claims.get("userId", Long.class);
                System.out.println("Token Claims" + claims);
                // for (Object elem : claims.values()) {
                //     System.out.println(elem);
                // }
                AuthUser user = new AuthUser(user_id, username);

                Authentication auth;
                auth = new UsernamePasswordAuthenticationToken(
                        user, // username as principal
                        null, // no credentials
                        List.of() // no authorities (add if you need role-based auth)
                );
                
                SecurityContextHolder.getContext().setAuthentication(auth);
                // System.out.println( (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal().getId());
                
            }
        }

        filterChain.doFilter(request, response);
    }
}
