package com.example.prathiphala_family_league.match.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.match.dto.MatchResultResponse;
import com.example.prathiphala_family_league.match.dto.PublishResultRequest;
import com.example.prathiphala_family_league.match.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matches/{matchId}/result")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @PostMapping
    @PreAuthorize("hasAuthority('PUBLISH_RESULT')")
    public ResponseEntity<ApiResponse<MatchResultResponse>> publish(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PublishResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resultService.publishResult(matchId, request, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MatchResultResponse>> getResult(@PathVariable Long matchId) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getResult(matchId)));
    }
}
