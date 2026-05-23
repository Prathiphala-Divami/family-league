package com.example.prathiphala_family_league.match.repository;

import com.example.prathiphala_family_league.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.prathiphala_family_league.match.entity.MatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // Used by PredictionReminderScheduler to find matches whose lock time is approaching.
    List<Match> findByPredictionLockTimeBetweenAndStatusAndDeletedFalse(
            Instant from, Instant to, MatchStatus status);

    // Used by ResultAlertScheduler to find matches that were completed but lack a result record.
    @Query(value = "SELECT m.* FROM match m " +
                   "WHERE m.status = 'COMPLETED' AND m.is_deleted = false " +
                   "AND NOT EXISTS (SELECT 1 FROM match_result mr WHERE mr.match_id = m.id AND mr.is_deleted = false)",
           nativeQuery = true)
    List<Match> findCompletedMatchesWithoutResult();

    @Query("SELECT m FROM Match m JOIN FETCH m.season JOIN FETCH m.team1 JOIN FETCH m.team2 " +
           "WHERE m.id = :id AND m.deleted = false")
    Optional<Match> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query(value = "SELECT m FROM Match m JOIN FETCH m.season JOIN FETCH m.team1 JOIN FETCH m.team2 " +
                   "WHERE m.season.id = :seasonId AND m.deleted = false",
           countQuery = "SELECT COUNT(m) FROM Match m WHERE m.season.id = :seasonId AND m.deleted = false")
    Page<Match> findBySeasonIdAndDeletedFalse(@Param("seasonId") Long seasonId, Pageable pageable);

    // Used to recalculate season.predictionLockTime when matches are added/cancelled.
    @Query("SELECT MIN(m.startTime) FROM Match m " +
           "WHERE m.season.id = :seasonId AND m.deleted = false AND m.status <> 'CANCELLED'")
    Optional<Instant> findEarliestStartTimeBySeasonId(@Param("seasonId") Long seasonId);
}
