package com.example.prathiphala_family_league.prediction.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "prediction")
public class Prediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_winner_team_id")
    private Team predictedWinnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_toss_winner_id")
    private Team predictedTossWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predicted_player_of_match_id")
    private Player predictedPlayerOfMatch;

    // System-calculated on result publish — never accepted from client (BR-S4).
    @Column(name = "points_earned", nullable = false)
    private int pointsEarned = 0;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
}
