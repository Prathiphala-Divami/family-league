package com.example.prathiphala_family_league.prediction.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "league_prediction")
public class LeaguePrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "predicted_rank", nullable = false)
    private int predictedRank;
}
