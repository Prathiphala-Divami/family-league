package com.example.prathiphala_family_league.match.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResultPublishedEvent {
    private final Long matchId;
    private final Long seasonId;
}
