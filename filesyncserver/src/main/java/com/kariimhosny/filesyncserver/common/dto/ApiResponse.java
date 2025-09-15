package com.kariimhosny.filesyncserver.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic API response wrapper for standardized JSON responses
 * @param <T> The type of data contained in the response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private List<T> data;
    
    /**
     * Create a success response with a list of data items and a message
     * 
     * @param <T> The type of data
     * @param data The list of data items
     * @param message The success message
     * @return A success response
     */
    public static <T> ApiResponse<T> success(List<T> data, String message) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * Create a success response with a single data item and a message
     * 
     * @param <T> The type of data
     * @param item The single data item
     * @param message The success message
     * @return A success response
     */
    public static <T> ApiResponse<T> success(T item, String message) {
        if (item == null) {
            return new ApiResponse<>(true, message, Collections.emptyList());
        }
        return new ApiResponse<>(true, message, Collections.singletonList(item));
    }
    
    /**
     * Create a success response with a list of data items and a default message
     * 
     * @param <T> The type of data
     * @param data The list of data items
     * @return A success response
     */
    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    /**
     * Create a success response with a single data item and a default message
     * 
     * @param <T> The type of data
     * @param item The single data item
     * @return A success response
     */
    public static <T> ApiResponse<T> success(T item) {
        if (item == null) {
            return new ApiResponse<>(true, "Success", Collections.emptyList());
        }
        return new ApiResponse<>(true, "Success", Collections.singletonList(item));
    }
    
    /**
     * Create an error response with a message and an empty data list
     * 
     * @param <T> The type of data
     * @param message The error message
     * @return An error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, Collections.emptyList());
    }
}