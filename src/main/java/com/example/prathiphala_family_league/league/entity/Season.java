package com.example.prathiphala_family_league.league.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "season")
public class Season extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SeasonStatus status = SeasonStatus.UPCOMING;

    // System-set: 4 hrs before the first match's start_time.
    // Null until the first match is scheduled for this season.
    @Column(name = "prediction_lock_time")
    private Instant predictionLockTime;
}
