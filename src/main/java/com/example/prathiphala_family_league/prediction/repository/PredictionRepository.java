package com.example.prathiphala_family_league.prediction.repository;

import com.example.prathiphala_family_league.prediction.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Used for upsert: must check regardless of deleted, because of DB UNIQUE constraint on (user_id, match_id).
    Optional<Prediction> findByMatchIdAndUserId(Long matchId, Long userId);

    @Query("SELECT p FROM Prediction p " +
           "LEFT JOIN FETCH p.predictedWinnerTeam " +
           "LEFT JOIN FETCH p.predictedTossWinner " +
           "LEFT JOIN FETCH p.predictedPlayerOfMatch " +
           "WHERE p.match.id = :matchId AND p.user.id = :userId AND p.deleted = false")
    Optional<Prediction> findMyPrediction(@Param("matchId") Long matchId, @Param("userId") Long userId);

    @Query("SELECT p FROM Prediction p " +
           "LEFT JOIN FETCH p.predictedWinnerTeam " +
           "LEFT JOIN FETCH p.predictedTossWinner " +
           "LEFT JOIN FETCH p.predictedPlayerOfMatch " +
           "WHERE p.match.id = :matchId AND p.deleted = false")
    List<Prediction> findMatchPredictions(@Param("matchId") Long matchId);
}
