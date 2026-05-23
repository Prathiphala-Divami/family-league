package com.example.prathiphala_family_league.league.repository;

import com.example.prathiphala_family_league.league.entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByIdAndDeletedFalse(Long id);
    Page<Season> findByLeagueIdAndDeletedFalse(Long leagueId, Pageable pageable);
}
