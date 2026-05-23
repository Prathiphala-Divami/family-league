package com.example.prathiphala_family_league.match.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.team.entity.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "match")
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    @Column(name = "venue", length = 255)
    private String venue;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    // Always startTime minus 1 hour — set by service, never accepted from client.
    @Column(name = "prediction_lock_time", nullable = false)
    private Instant predictionLockTime;

    @Column(name = "match_number")
    private Integer matchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MatchStatus status = MatchStatus.SCHEDULED;
}
