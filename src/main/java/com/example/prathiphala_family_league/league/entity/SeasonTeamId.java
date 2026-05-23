package com.example.prathiphala_family_league.league.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SeasonTeamId implements Serializable {

    @Column(name = "season_id")
    private Long seasonId;

    @Column(name = "team_id")
    private Long teamId;
}
