package com.example.prathiphala_family_league.prediction.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.prediction.dto.PredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitPredictionRequest;
import com.example.prathiphala_family_league.prediction.service.PredictionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Predictions", description = "Submit and view match predictions")
@RestController
@RequestMapping("/api/v1/matches/{matchId}/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PredictionResponse>> submit(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitPredictionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(predictionService.submitOrUpdate(userId, matchId, request)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PredictionResponse>> update(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SubmitPredictionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.submitOrUpdate(userId, matchId, request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PredictionResponse>> getMyPrediction(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getMyPrediction(userId, matchId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getAllPredictions(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.getAllPredictions(matchId, userId)));
    }
}
