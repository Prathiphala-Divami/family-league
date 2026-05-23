package com.example.prathiphala_family_league.league.service;

import com.example.prathiphala_family_league.league.dto.CreateSeasonRequest;
import com.example.prathiphala_family_league.league.dto.SeasonResponse;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.league.entity.SeasonStatus;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SeasonService {
    SeasonResponse create(Long leagueId, CreateSeasonRequest request);
    Page<SeasonResponse> getByLeague(Long leagueId, Pageable pageable);
    SeasonResponse getById(Long id);
    SeasonResponse transitionStatus(Long seasonId, SeasonStatus newStatus);
    SeasonResponse closeSeason(Long seasonId);
    SeasonResponse addTeamToSeason(Long seasonId, Long teamId);
    List<TeamResponse> getTeamsInSeason(Long seasonId);

    // Package-visible: lets other services (Match, Prediction) load a Season entity
    Season findSeason(Long seasonId);
}
