package com.kariimhosny.filesyncserver.crypto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing user's public keys for cryptographic operations. Each
 * user can have multiple keys to support key rotation.
 */
@Entity
@Table(name = "user_keys", indexes = {
    @Index(name = "idx_user_keys_user_id", columnList = "user_id"),
    @Index(name = "idx_user_keys_active", columnList = "user_id, is_active")
})
public class UserKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Public key cannot be blank")
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public UserKey() {
    }

    public UserKey(Long userId, String publicKey) {
        this.userId = userId;
        this.publicKey = publicKey;
        this.isActive = true;
    }

    public UserKey(Long userId, String publicKey, Boolean isActive) {
        this.userId = userId;
        this.publicKey = publicKey;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    /**
     * Activates this key and optionally deactivates others for the same user
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates this key
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Checks if this key is currently active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Validates if the public key format is correct (basic validation)
     */
    public boolean isValidKeyFormat() {
        return publicKey != null
                && !publicKey.trim().isEmpty()
                && (publicKey.contains("BEGIN PUBLIC KEY") || publicKey.length() > 100);
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserKey userKey = (UserKey) obj;
        return Objects.equals(id, userKey.id)
                && Objects.equals(userId, userKey.userId)
                && Objects.equals(publicKey, userKey.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, publicKey);
    }

    @Override
    public String toString() {
        return String.format("UserKey{id=%d, userId=%d, isActive=%s, createdAt=%s}",
                id, userId, isActive, createdAt);
    }
}
