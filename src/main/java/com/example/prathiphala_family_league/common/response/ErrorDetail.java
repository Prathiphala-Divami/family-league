package com.example.prathiphala_family_league.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor(staticName = "of")
public class ErrorDetail {

    private final String code;
    private final String message;
    private final String path;
    private final List<FieldError> fieldErrors;

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
