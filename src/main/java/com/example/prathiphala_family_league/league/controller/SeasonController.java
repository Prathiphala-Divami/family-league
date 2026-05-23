package com.example.prathiphala_family_league.league.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.league.dto.AddSeasonTeamRequest;
import com.example.prathiphala_family_league.league.dto.CreateSeasonRequest;
import com.example.prathiphala_family_league.league.dto.SeasonResponse;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    // ── Seasons under a league ────────────────────────────────

    @PostMapping("/api/v1/leagues/{leagueId}/seasons")
    @PreAuthorize("hasAuthority('CREATE_SEASON')")
    public ResponseEntity<ApiResponse<SeasonResponse>> create(
            @PathVariable Long leagueId,
            @Valid @RequestBody CreateSeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(seasonService.create(leagueId, request)));
    }

    @GetMapping("/api/v1/leagues/{leagueId}/seasons")
    public ResponseEntity<ApiResponse<PagedResponse<SeasonResponse>>> getByLeague(
            @PathVariable Long leagueId,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(seasonService.getByLeague(leagueId, pageable))));
    }

    // ── Season-level operations ───────────────────────────────

    @GetMapping("/api/v1/seasons/{id}")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.getById(id)));
    }

    @PutMapping("/api/v1/seasons/{id}/close")
    @PreAuthorize("hasAuthority('CLOSE_SEASON')")
    public ResponseEntity<ApiResponse<SeasonResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.closeSeason(id)));
    }

    // ── Season-team roster ────────────────────────────────────

    @PostMapping("/api/v1/seasons/{id}/teams")
    @PreAuthorize("hasAuthority('MANAGE_SEASON_TEAMS')")
    public ResponseEntity<ApiResponse<SeasonResponse>> addTeam(
            @PathVariable Long id,
            @Valid @RequestBody AddSeasonTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(seasonService.addTeamToSeason(id, request.getTeamId())));
    }

    @GetMapping("/api/v1/seasons/{id}/teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeams(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.getTeamsInSeason(id)));
    }
}
