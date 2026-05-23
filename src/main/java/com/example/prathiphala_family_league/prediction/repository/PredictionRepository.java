package com.example.prathiphala_family_league.prediction.repository;

import com.example.prathiphala_family_league.prediction.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Aggregates total points per user for a season — used by LeaderboardService.recalculate().
    @Query(value = "SELECT p.user_id AS userId, COALESCE(SUM(p.points_earned), 0) AS totalPoints " +
                   "FROM prediction p JOIN match m ON m.id = p.match_id " +
                   "WHERE m.season_id = :seasonId AND p.is_deleted = false " +
                   "GROUP BY p.user_id",
           nativeQuery = true)
    List<UserPointsProjection> sumPointsByUserForSeason(@Param("seasonId") Long seasonId);

    // Returns user IDs who already have a prediction for a match — used by the reminder scheduler.
    @Query("SELECT p.user.id FROM Prediction p WHERE p.match.id = :matchId AND p.deleted = false")
    Set<Long> findUserIdsWithPredictionForMatch(@Param("matchId") Long matchId);

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
