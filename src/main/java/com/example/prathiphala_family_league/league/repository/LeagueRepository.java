package com.example.prathiphala_family_league.league.repository;

import com.example.prathiphala_family_league.league.entity.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByIdAndDeletedFalse(Long id);

    @Query("SELECT l FROM League l WHERE l.deleted = false AND " +
           "(:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<League> searchByName(@Param("search") String search, Pageable pageable);
}
