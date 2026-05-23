package com.example.prathiphala_family_league.prediction.service;

import com.example.prathiphala_family_league.prediction.dto.LeaguePredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitLeaguePredictionRequest;

public interface LeaguePredictionService {
    LeaguePredictionResponse submitOrUpdate(Long userId, Long seasonId, SubmitLeaguePredictionRequest request);
    LeaguePredictionResponse getMyPredictions(Long userId, Long seasonId);
}
