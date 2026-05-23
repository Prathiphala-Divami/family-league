package com.example.prathiphala_family_league.league.service;

import com.example.prathiphala_family_league.league.dto.CreateLeagueRequest;
import com.example.prathiphala_family_league.league.dto.LeagueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeagueService {
    LeagueResponse create(CreateLeagueRequest request);
    Page<LeagueResponse> getAll(Pageable pageable, String search);
    LeagueResponse getById(Long id);
}
