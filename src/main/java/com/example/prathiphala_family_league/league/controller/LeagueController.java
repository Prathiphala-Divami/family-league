package com.example.prathiphala_family_league.league.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.league.dto.CreateLeagueRequest;
import com.example.prathiphala_family_league.league.dto.LeagueResponse;
import com.example.prathiphala_family_league.league.service.LeagueService;
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
@RequestMapping("/api/v1/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_LEAGUE')")
    public ResponseEntity<ApiResponse<LeagueResponse>> create(@Valid @RequestBody CreateLeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leagueService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LeagueResponse>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(leagueService.getAll(pageable, search))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeagueResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getById(id)));
    }
}
