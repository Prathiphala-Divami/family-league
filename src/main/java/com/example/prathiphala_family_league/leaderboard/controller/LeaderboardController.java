package com.example.prathiphala_family_league.leaderboard.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.leaderboard.dto.LeaderboardEntryResponse;
import com.example.prathiphala_family_league.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Leaderboard", description = "View season leaderboard with DENSE_RANK scoring")
@RestController
@RequestMapping("/api/v1/seasons/{seasonId}/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<LeaderboardEntryResponse>>> getLeaderboard(
            @PathVariable Long seasonId,
            @PageableDefault(size = 20, sort = "rankPosition", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(leaderboardService.getLeaderboard(seasonId, pageable))));
    }
}
