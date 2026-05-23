package com.example.prathiphala_family_league.leaderboard.repository;

import com.example.prathiphala_family_league.leaderboard.entity.Leaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    Optional<Leaderboard> findBySeasonIdAndUserId(Long seasonId, Long userId);

    List<Leaderboard> findBySeasonIdOrderByTotalPointsDesc(Long seasonId);

    Page<Leaderboard> findBySeasonIdOrderByRankPositionAsc(Long seasonId, Pageable pageable);

    // PostgreSQL upsert — inserts a new row or updates total_points on conflict.
    @Modifying
    @Query(value = "INSERT INTO leaderboard (season_id, user_id, total_points, updated_at) " +
                   "VALUES (:seasonId, :userId, :totalPoints, NOW()) " +
                   "ON CONFLICT (season_id, user_id) DO UPDATE SET total_points = :totalPoints, updated_at = NOW()",
           nativeQuery = true)
    void upsertTotalPoints(@Param("seasonId") Long seasonId,
                           @Param("userId") Long userId,
                           @Param("totalPoints") int totalPoints);
}
