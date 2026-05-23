package com.example.prathiphala_family_league.match.service;

import com.example.prathiphala_family_league.match.dto.MatchResultResponse;
import com.example.prathiphala_family_league.match.dto.PublishResultRequest;

public interface ResultService {
    MatchResultResponse publishResult(Long matchId, PublishResultRequest request, Long publisherId);
    MatchResultResponse getResult(Long matchId);
}
