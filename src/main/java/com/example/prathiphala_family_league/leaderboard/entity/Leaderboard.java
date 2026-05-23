package com.example.prathiphala_family_league.leaderboard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// Does NOT extend BaseEntity — this is an aggregate/computed table, rows are upserted.
@Getter
@Setter
@Entity
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
