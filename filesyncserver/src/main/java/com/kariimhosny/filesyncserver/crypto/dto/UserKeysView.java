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

public interface UserKeysView {
    String getPublicKey();
    Long getUserId();
}