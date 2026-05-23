package com.example.prathiphala_family_league.match.repository;

import com.example.prathiphala_family_league.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

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
