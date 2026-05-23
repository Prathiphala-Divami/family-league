package com.example.prathiphala_family_league.team.entity;

import com.example.prathiphala_family_league.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "team")
public class Team extends BaseEntity {

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "short_name", length = 10)
    private String shortName;

    @Column(name = "logo", length = 500)
    private String logo;
}
