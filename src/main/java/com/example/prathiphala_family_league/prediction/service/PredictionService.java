package com.example.prathiphala_family_league.prediction.service;

import com.example.prathiphala_family_league.prediction.dto.PredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitPredictionRequest;

import java.util.List;

public interface PredictionService {
    PredictionResponse submitOrUpdate(Long userId, Long matchId, SubmitPredictionRequest request);
    PredictionResponse getMyPrediction(Long userId, Long matchId);

    // Returns only caller's prediction before lock; all predictions after lock.
    List<PredictionResponse> getAllPredictions(Long matchId, Long callerUserId);
}
