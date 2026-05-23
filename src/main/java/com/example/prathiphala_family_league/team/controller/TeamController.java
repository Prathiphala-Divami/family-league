package com.example.prathiphala_family_league.team.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.team.dto.CreateTeamRequest;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import com.example.prathiphala_family_league.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TEAMS')")
    public ResponseEntity<ApiResponse<TeamResponse>> create(
            @Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(teamService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TeamResponse>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "teamName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(teamService.getAll(pageable, search))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEAMS')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
