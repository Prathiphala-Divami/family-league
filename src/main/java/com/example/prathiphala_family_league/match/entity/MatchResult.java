package com.example.prathiphala_family_league.match.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
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
@Table(name = "match_result")
public class MatchResult extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 20)
    private ResultType resultType;

    // Null for TIE and NO_RESULT.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_team_id")
    private Team winningTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_team_id")
    private Team tossWinnerTeam;

    // Null for TIE and NO_RESULT.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_of_match_id")
    private Player playerOfMatch;

    @Column(name = "winning_margin", length = 100)
    private String winningMargin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", nullable = false)
    private User publishedBy;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
}
