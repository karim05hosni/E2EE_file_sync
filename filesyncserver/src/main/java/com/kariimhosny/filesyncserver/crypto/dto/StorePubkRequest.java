package com.kariimhosny.filesyncserver.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for storing user's cryptographic keys
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorePubkRequest {
    @NotBlank(message = "Public key is required")
    @Size(min = 100, message = "Public key appears to be too short")
    private String publicKey;
    /**
     * Validates if the public key format appears correct (basic validation)
     * @return true if the key format appears valid
     */
    public boolean isValidPublicKeyFormat() {
        return publicKey != null 
                && !publicKey.trim().isEmpty()
                && (publicKey.contains("BEGIN PUBLIC KEY") || publicKey.length() > 100);
    }
}
