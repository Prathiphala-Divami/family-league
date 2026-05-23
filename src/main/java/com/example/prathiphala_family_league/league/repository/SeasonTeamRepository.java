package com.example.prathiphala_family_league.league.repository;

import com.example.prathiphala_family_league.league.entity.SeasonTeam;
import com.example.prathiphala_family_league.league.entity.SeasonTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SeasonTeamRepository extends JpaRepository<SeasonTeam, SeasonTeamId> {

    @Query("SELECT st FROM SeasonTeam st WHERE st.id.seasonId = :seasonId")
    List<SeasonTeam> findBySeasonId(@Param("seasonId") Long seasonId);

    @Query("SELECT st.id.teamId FROM SeasonTeam st WHERE st.id.seasonId = :seasonId")
    Set<Long> findTeamIdsBySeasonId(@Param("seasonId") Long seasonId);

    boolean existsById(SeasonTeamId id);
}
