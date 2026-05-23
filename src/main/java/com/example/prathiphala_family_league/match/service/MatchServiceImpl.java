package com.example.prathiphala_family_league.match.service;

import com.example.prathiphala_family_league.common.exception.InvalidSeasonTeamException;
import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.common.exception.UnauthorisedActionException;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.league.entity.SeasonTeamId;
import com.example.prathiphala_family_league.league.repository.SeasonTeamRepository;
import com.example.prathiphala_family_league.league.service.SeasonGuard;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.match.dto.CreateMatchRequest;
import com.example.prathiphala_family_league.match.dto.MatchResponse;
import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.entity.MatchStatus;
import com.example.prathiphala_family_league.match.repository.MatchRepository;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final SeasonService seasonService;
    private final TeamService teamService;
    private final SeasonTeamRepository seasonTeamRepository;

    @Override
    @Transactional
    public MatchResponse create(Long seasonId, CreateMatchRequest request) {
        Season season = seasonService.findSeason(seasonId);
        SeasonGuard.assertNotClosed(season);

        if (request.getTeam1Id().equals(request.getTeam2Id())) {
            throw new InvalidSeasonTeamException("A match cannot be scheduled between the same team");
        }

        Team team1 = teamService.findTeam(request.getTeam1Id());
        Team team2 = teamService.findTeam(request.getTeam2Id());

        assertTeamInSeason(seasonId, request.getTeam1Id(), team1.getTeamName());
        assertTeamInSeason(seasonId, request.getTeam2Id(), team2.getTeamName());

        Match match = new Match();
        match.setSeason(season);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setVenue(request.getVenue());
        match.setStartTime(request.getStartTime());
        // prediction_lock_time is always startTime - 1 hour; never accepted from client
        match.setPredictionLockTime(request.getStartTime().minus(1, ChronoUnit.HOURS));
        match.setMatchNumber(request.getMatchNumber());
        match.setStatus(MatchStatus.SCHEDULED);

        Match saved = matchRepository.save(match);

        // Keep season.predictionLockTime = 4 hours before the earliest scheduled match
        refreshSeasonPredictionLockTime(seasonId);

        return new MatchResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchResponse> getBySeason(Long seasonId, Pageable pageable) {
        seasonService.findSeason(seasonId);
        return matchRepository.findBySeasonIdAndDeletedFalse(seasonId, pageable)
                .map(MatchResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getById(Long id) {
        return new MatchResponse(findMatch(id));
    }

    @Override
    @Transactional
    public MatchResponse cancel(Long id) {
        Match match = findMatch(id);
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new UnauthorisedActionException(
                    "Only SCHEDULED matches can be cancelled, current status: " + match.getStatus());
        }
        match.setStatus(MatchStatus.CANCELLED);
        MatchResponse response = new MatchResponse(matchRepository.save(match));

        // Re-evaluate season lock time now that this match is cancelled
        refreshSeasonPredictionLockTime(match.getSeason().getId());

        return response;
    }

    @Override
    @Transactional
    public void markCompleted(Long matchId) {
        Match match = findMatch(matchId);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
    }

    @Override
    public Match findMatch(Long id) {
        return matchRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    private void assertTeamInSeason(Long seasonId, Long teamId, String teamName) {
        if (!seasonTeamRepository.existsById(new SeasonTeamId(seasonId, teamId))) {
            throw new InvalidSeasonTeamException(
                    "Team '" + teamName + "' is not enrolled in season " + seasonId);
        }
    }

    // Recalculates season.predictionLockTime = 4h before the earliest non-cancelled match.
    private void refreshSeasonPredictionLockTime(Long seasonId) {
        Optional<Instant> earliest = matchRepository.findEarliestStartTimeBySeasonId(seasonId);
        Instant lockTime = earliest.map(t -> t.minus(4, ChronoUnit.HOURS)).orElse(null);
        seasonService.updatePredictionLockTime(seasonId, lockTime);
    }
}
