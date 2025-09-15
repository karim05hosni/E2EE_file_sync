package com.kariimhosny.filesyncserver.auth.services.impl;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.kariimhosny.filesyncserver.auth.enrtities.User;
import com.kariimhosny.filesyncserver.auth.services.contracts.IJWTServices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JWTServices implements IJWTServices {
    private final String privateKeyPath;
    private final String publicKeyPath;

    public JWTServices (@Value("${app.jwt.privateKeyPath}") String privateKeyPath,
    @Value("${app.jwt.publicKeyPath}") String publicKeyPath) {
    this.privateKeyPath = privateKeyPath;
    this.publicKeyPath = publicKeyPath;
    }



    // load the private key to a format that app can understand
    public PrivateKey getPrivateKey() {
        try {
            // reads the privK file into String
            ClassPathResource resource = new ClassPathResource(privateKeyPath.replace("classpath:", ""));
            String privateKeyContent = new String(resource.getInputStream().readAllBytes());
            // wrapping the key ( remove header & footer )
            privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            // convert from Base64 format to raw bytes (standard for cryptographic libraries)
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

            // convert to PKCS8 ( standard format for cryptographic libraries)
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            // java class to do cryptographic operations
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }
    
    @Override
    public String issueToken(User user) {
        PrivateKey privK = getPrivateKey();
        System.out.println("user entity from issue token");
        System.out.println(user);
        return Jwts.builder()
            .subject(user.getId().toString()) // Unique user identifier
            .claim("username", user.getName()) // Additional claims
            .claim("email", user.getEmail())
            .claim("userId", user.getId())
            .claim("spaceId", user.getSpaceId() != null ? user.getSpaceId().toString() : null)
            .issuedAt(new Date()) // Token creation time
            .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours expiration
            .signWith(privK, Jwts.SIG.RS256) // Explicit algorithm specification
            .compact();
    }

    // Load the public key to validate tokens
    public PublicKey getPublicKey() {
        try {
            // Read the public key file
            ClassPathResource resource = new ClassPathResource(publicKeyPath.replace("classpath:", ""));
            String publicKeyContent = new String(resource.getInputStream().readAllBytes());
            
            // Remove header, footer and whitespace
            publicKeyContent = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            
            // Decode from Base64
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            
            // Create X509 key spec for public keys
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            
            // Generate the public key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }
    
    public boolean isValidToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(getPublicKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }
    
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
