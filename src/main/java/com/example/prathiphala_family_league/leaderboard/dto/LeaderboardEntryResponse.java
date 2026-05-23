package com.example.prathiphala_family_league.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private final int rankPosition;
    private final Long userId;
    private final String userName;
    private final int totalPoints;
}
