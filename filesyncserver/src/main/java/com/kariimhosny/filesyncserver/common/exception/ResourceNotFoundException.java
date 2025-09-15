package com.kariimhosny.filesyncserver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Create a new ResourceNotFoundException with a message
     * 
     * @param message The error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Create a new ResourceNotFoundException with a message and cause
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Create a new ResourceNotFoundException for a specific resource type and ID
     * 
     * @param resourceName The name of the resource type
     * @param fieldName The name of the identifier field
     * @param fieldValue The value of the identifier
     * @return A new ResourceNotFoundException
     */
    public static ResourceNotFoundException forResource(String resourceName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(resourceName + " not found with " + fieldName + ": " + fieldValue);
    }
}