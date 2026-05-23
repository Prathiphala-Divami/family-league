package com.example.prathiphala_family_league.team.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "player")
public class Player extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_role", length = 30)
    private PlayerRole playerRole;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "country", length = 100)
    private String country;
}
