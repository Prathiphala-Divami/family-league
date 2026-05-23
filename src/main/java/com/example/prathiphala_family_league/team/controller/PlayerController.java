package com.example.prathiphala_family_league.team.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.team.dto.CreatePlayerRequest;
import com.example.prathiphala_family_league.team.dto.PlayerResponse;
import com.example.prathiphala_family_league.team.dto.UpdatePlayerRequest;
import com.example.prathiphala_family_league.team.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_PLAYERS')")
    public ResponseEntity<ApiResponse<PlayerResponse>> addPlayer(
            @PathVariable Long teamId,
            @Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(playerService.addPlayer(teamId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerResponse>>> getPlayers(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(playerService.getPlayersByTeam(teamId)));
    }

    @PutMapping("/{playerId}")
    @PreAuthorize("hasAuthority('MANAGE_PLAYERS')")
    public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @Valid @RequestBody UpdatePlayerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                playerService.updatePlayer(teamId, playerId, request)));
    }

    @DeleteMapping("/{playerId}")
    @PreAuthorize("hasAuthority('MANAGE_PLAYERS')")
    public ResponseEntity<ApiResponse<Void>> removePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        playerService.removePlayer(teamId, playerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
