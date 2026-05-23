package com.example.prathiphala_family_league.prediction.dto;

import com.example.prathiphala_family_league.prediction.entity.LeaguePrediction;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@Getter
public class LeaguePredictionResponse {

    private final Long seasonId;
    private final String seasonName;
    private final Long userId;
    private final List<RankEntry> rankings;

    public LeaguePredictionResponse(List<LeaguePrediction> predictions) {
        if (predictions.isEmpty()) {
            this.seasonId = null;
            this.seasonName = null;
            this.userId = null;
            this.rankings = List.of();
            return;
        }
        LeaguePrediction first = predictions.get(0);
        this.seasonId = first.getSeason().getId();
        this.seasonName = first.getSeason().getName();
        this.userId = first.getUser().getId();
        this.rankings = predictions.stream()
                .sorted(Comparator.comparingInt(LeaguePrediction::getPredictedRank))
                .map(lp -> new RankEntry(
                        lp.getPredictedRank(),
                        lp.getTeam().getId(),
                        lp.getTeam().getTeamName(),
                        lp.getTeam().getShortName()))
                .toList();
    }

    @Getter
    public static class RankEntry {
        private final int rank;
        private final Long teamId;
        private final String teamName;
        private final String shortName;

        public RankEntry(int rank, Long teamId, String teamName, String shortName) {
            this.rank = rank;
            this.teamId = teamId;
            this.teamName = teamName;
            this.shortName = shortName;
        }
    }
}
