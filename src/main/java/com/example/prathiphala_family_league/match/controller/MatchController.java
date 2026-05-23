package com.example.prathiphala_family_league.match.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.match.dto.CreateMatchRequest;
import com.example.prathiphala_family_league.match.dto.MatchResponse;
import com.example.prathiphala_family_league.match.service.MatchService;
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
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // ── Matches under a season ────────────────────────────────

    @PostMapping("/api/v1/seasons/{seasonId}/matches")
    @PreAuthorize("hasAuthority('CREATE_MATCH')")
    public ResponseEntity<ApiResponse<MatchResponse>> create(
            @PathVariable Long seasonId,
            @Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(matchService.create(seasonId, request)));
    }

    @GetMapping("/api/v1/seasons/{seasonId}/matches")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> getBySeason(
            @PathVariable Long seasonId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(matchService.getBySeason(seasonId, pageable))));
    }

    // ── Match-level operations ────────────────────────────────

    @GetMapping("/api/v1/matches/{id}")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getById(id)));
    }

    @PutMapping("/api/v1/matches/{id}/cancel")
    @PreAuthorize("hasAuthority('CREATE_MATCH')")
    public ResponseEntity<ApiResponse<MatchResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(matchService.cancel(id)));
    }
}
