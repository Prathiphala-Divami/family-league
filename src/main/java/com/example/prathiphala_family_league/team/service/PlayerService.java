package com.example.prathiphala_family_league.team.service;

import com.example.prathiphala_family_league.team.dto.CreatePlayerRequest;
import com.example.prathiphala_family_league.team.dto.PlayerResponse;
import com.example.prathiphala_family_league.team.dto.UpdatePlayerRequest;
import com.example.prathiphala_family_league.team.entity.Player;

import java.util.List;

public interface PlayerService {
    PlayerResponse addPlayer(Long teamId, CreatePlayerRequest request);
    List<PlayerResponse> getPlayersByTeam(Long teamId);
    PlayerResponse updatePlayer(Long teamId, Long playerId, UpdatePlayerRequest request);
    void removePlayer(Long teamId, Long playerId);

    // Entity accessor for other services (Prediction, MatchResult)
    Player findPlayer(Long playerId);
}
