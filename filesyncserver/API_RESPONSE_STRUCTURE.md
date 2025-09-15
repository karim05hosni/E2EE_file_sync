# Standardized API Response Structure

## Overview

This document describes the standardized API response structure implemented across the File Sync application. All API endpoints now return responses in a consistent JSON format to improve client-side integration and error handling.

## Response Format

All API responses follow this structure:

```json
{
  "message": "string describing the outcome",
  "success": boolean,
  "data": [ array of objects or empty array ]
}
```

### Fields

- **message**: A human-readable description of the operation result
- **success**: Boolean indicating if the operation was successful
- **data**: Array containing the response payload (may be empty for error responses or operations that don't return data)

## Implementation Details

### ApiResponse Class

The `ApiResponse<T>` class in `com.kariimhosny.filesyncserver.common.dto` provides the implementation for this structure:

- Generic type `<T>` allows for type-safe responses
- Factory methods simplify creating consistent responses:
  - `ApiResponse.success(T data, String message)`
  - `ApiResponse.success(List<T> data, String message)`
  - `ApiResponse.error(String message)`

### Exception Handling

The `GlobalExceptionHandler` class in `com.kariimhosny.filesyncserver.common.exception` ensures that all exceptions are converted to the standardized response format:

- Validation errors include field-specific error messages
- Resource not found exceptions return appropriate 404 status codes
- General exceptions are handled with 500 status codes

## Usage Examples

### Success Response

```java
// Single item response
return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));

// List response
return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
```

### Error Response

```java
return ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(ApiResponse.error("Invalid request parameters"));
```

## Client Integration

Clients should:

1. Check the `success` field to determine if the operation succeeded
2. Read the `message` field for human-readable information
3. Process the `data` array for response content (when success is true)

## Future Considerations

- Consider adding pagination metadata for list responses
- Add request tracing IDs for better debugging
- Implement response compression for large payloads