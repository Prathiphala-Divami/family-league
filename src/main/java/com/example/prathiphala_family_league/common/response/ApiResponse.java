package com.example.prathiphala_family_league.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;
    private final Instant timestamp;

    private ApiResponse(boolean success, T data, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, ErrorDetail.of(code, message, null, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, String path) {
        return new ApiResponse<>(false, null, ErrorDetail.of(code, message, path, null));
    }

    public static <T> ApiResponse<T> validationError(List<ErrorDetail.FieldError> fieldErrors) {
        return new ApiResponse<>(false, null, ErrorDetail.of("VALIDATION_ERROR", "Input validation failed", null, fieldErrors));
    }
}
