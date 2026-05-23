package com.example.prathiphala_family_league.prediction.repository;

import com.example.prathiphala_family_league.prediction.entity.LeaguePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaguePredictionRepository extends JpaRepository<LeaguePrediction, Long> {

    @Query("SELECT lp FROM LeaguePrediction lp JOIN FETCH lp.team " +
           "WHERE lp.user.id = :userId AND lp.season.id = :seasonId AND lp.deleted = false " +
           "ORDER BY lp.predictedRank")
    List<LeaguePrediction> findByUserIdAndSeasonId(@Param("userId") Long userId,
                                                    @Param("seasonId") Long seasonId);

    // Hard-delete previous submission before re-inserting — required because the DB's UNIQUE
    // constraints on (user_id, season_id, rank) and (user_id, season_id, team_id) do not
    // exclude soft-deleted rows, so upsert-via-soft-delete would violate them.
    @Modifying
    @Query("DELETE FROM LeaguePrediction lp WHERE lp.user.id = :userId AND lp.season.id = :seasonId")
    int deleteByUserIdAndSeasonId(@Param("userId") Long userId, @Param("seasonId") Long seasonId);
}
