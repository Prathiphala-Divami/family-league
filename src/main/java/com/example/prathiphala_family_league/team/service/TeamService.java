package com.example.prathiphala_family_league.team.service;

import com.example.prathiphala_family_league.team.dto.CreateTeamRequest;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import com.example.prathiphala_family_league.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    TeamResponse create(CreateTeamRequest request);
    Page<TeamResponse> getAll(Pageable pageable, String search);
    TeamResponse getById(Long id);
    void softDelete(Long id);

    // Entity accessor for other services (Player, Match, SeasonService)
    Team findTeam(Long id);
}
