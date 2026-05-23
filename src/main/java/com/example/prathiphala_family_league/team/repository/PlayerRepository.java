package com.example.prathiphala_family_league.team.repository;

import com.example.prathiphala_family_league.team.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p JOIN FETCH p.team WHERE p.team.id = :teamId AND p.deleted = false")
    List<Player> findByTeamIdAndDeletedFalse(@Param("teamId") Long teamId);

    Optional<Player> findByIdAndDeletedFalse(Long id);
}
