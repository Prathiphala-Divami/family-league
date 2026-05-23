package com.example.prathiphala_family_league.prediction.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.prediction.dto.LeaguePredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitLeaguePredictionRequest;
import com.example.prathiphala_family_league.prediction.service.LeaguePredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seasons/{seasonId}/league-predictions")
@RequiredArgsConstructor
public class LeaguePredictionController {

    private final LeaguePredictionService leaguePredictionService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaguePredictionResponse>> submit(
            @PathVariable Long seasonId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitLeaguePredictionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leaguePredictionService.submitOrUpdate(userId, seasonId, request)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<LeaguePredictionResponse>> update(
            @PathVariable Long seasonId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitLeaguePredictionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(leaguePredictionService.submitOrUpdate(userId, seasonId, request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LeaguePredictionResponse>> getMyPredictions(
            @PathVariable Long seasonId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(leaguePredictionService.getMyPredictions(userId, seasonId)));
    }
}
