package com.example.prathiphala_family_league.prediction.repository;

// Projection returned by PredictionRepository.sumPointsByUserForSeason (native query).
public interface UserPointsProjection {
    Long getUserId();
    Integer getTotalPoints();
}
