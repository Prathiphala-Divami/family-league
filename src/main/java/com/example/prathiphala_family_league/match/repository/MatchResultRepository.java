package com.example.prathiphala_family_league.match.repository;

import com.example.prathiphala_family_league.match.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    // Check regardless of deleted — match_id has a UNIQUE constraint in the DB.
    boolean existsByMatchId(Long matchId);

    @Query("SELECT r FROM MatchResult r " +
           "LEFT JOIN FETCH r.winningTeam " +
           "LEFT JOIN FETCH r.tossWinnerTeam " +
           "LEFT JOIN FETCH r.playerOfMatch " +
           "LEFT JOIN FETCH r.publishedBy " +
           "WHERE r.match.id = :matchId AND r.deleted = false")
    Optional<MatchResult> findByMatchIdAndDeletedFalse(@Param("matchId") Long matchId);
}
