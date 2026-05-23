package com.example.prathiphala_family_league.league.service;

import com.example.prathiphala_family_league.common.exception.SeasonClosedException;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.league.entity.SeasonStatus;

public final class SeasonGuard {

    private SeasonGuard() {}

    // Call at the start of every write operation on season-owned data.
    public static void assertNotClosed(Season season) {
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new SeasonClosedException(
                    "Season '" + season.getName() + "' is closed and no further changes are allowed");
        }
    }
}
