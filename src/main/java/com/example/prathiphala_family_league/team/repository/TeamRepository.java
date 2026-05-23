package com.example.prathiphala_family_league.team.repository;

import com.example.prathiphala_family_league.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByIdAndDeletedFalse(Long id);
    Page<Team> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.deleted = false AND " +
           "(:search IS NULL OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Team> searchByTeamName(@Param("search") String search, Pageable pageable);
}
