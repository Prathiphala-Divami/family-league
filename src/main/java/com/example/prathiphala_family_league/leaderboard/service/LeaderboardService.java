package com.example.prathiphala_family_league.leaderboard.service;

import com.example.prathiphala_family_league.leaderboard.dto.LeaderboardEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaderboardService {
    // Called async from ScoringService after each match result is scored.
    void recalculate(Long seasonId);

    Page<LeaderboardEntryResponse> getLeaderboard(Long seasonId, Pageable pageable);
}
