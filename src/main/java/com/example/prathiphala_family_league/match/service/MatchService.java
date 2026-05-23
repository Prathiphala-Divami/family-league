package com.example.prathiphala_family_league.match.service;

import com.example.prathiphala_family_league.match.dto.CreateMatchRequest;
import com.example.prathiphala_family_league.match.dto.MatchResponse;
import com.example.prathiphala_family_league.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchService {
    MatchResponse create(Long seasonId, CreateMatchRequest request);
    Page<MatchResponse> getBySeason(Long seasonId, Pageable pageable);
    MatchResponse getById(Long id);
    MatchResponse cancel(Long id);

    // Called by ResultService after a result is published.
    void markCompleted(Long matchId);

    // Entity accessor for other services (Prediction, MatchResult)
    Match findMatch(Long id);
}
