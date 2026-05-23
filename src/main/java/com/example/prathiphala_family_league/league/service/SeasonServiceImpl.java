package com.example.prathiphala_family_league.league.service;

import com.example.prathiphala_family_league.common.exception.InvalidSeasonTeamException;
import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.common.exception.UnauthorisedActionException;
import com.example.prathiphala_family_league.league.dto.CreateSeasonRequest;
import com.example.prathiphala_family_league.league.dto.SeasonResponse;
import com.example.prathiphala_family_league.league.entity.*;
import com.example.prathiphala_family_league.league.repository.SeasonRepository;
import com.example.prathiphala_family_league.league.repository.SeasonTeamRepository;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final LeagueServiceImpl leagueService;
    private final TeamRepository teamRepository;

    // Allowed status transitions
    private static final Map<SeasonStatus, SeasonStatus> TRANSITIONS = Map.of(
            SeasonStatus.UPCOMING,   SeasonStatus.ACTIVE,
            SeasonStatus.ACTIVE,     SeasonStatus.COMPLETED,
            SeasonStatus.COMPLETED,  SeasonStatus.CLOSED
    );

    @Override
    @Transactional
    public SeasonResponse create(Long leagueId, CreateSeasonRequest request) {
        League league = leagueService.findLeague(leagueId);

        Season season = new Season();
        season.setLeague(league);
        season.setName(request.getName());
        season.setStartDate(request.getStartDate());
        season.setEndDate(request.getEndDate());
        season.setStatus(SeasonStatus.UPCOMING);

        return new SeasonResponse(seasonRepository.save(season));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonResponse> getByLeague(Long leagueId, Pageable pageable) {
        leagueService.findLeague(leagueId); // validate league exists
        return seasonRepository.findByLeagueIdAndDeletedFalse(leagueId, pageable)
                .map(SeasonResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse getById(Long id) {
        return new SeasonResponse(findSeason(id));
    }

    @Override
    @Transactional
    public SeasonResponse transitionStatus(Long seasonId, SeasonStatus newStatus) {
        Season season = findSeason(seasonId);
        SeasonStatus allowed = TRANSITIONS.get(season.getStatus());
        if (allowed == null || allowed != newStatus) {
            throw new UnauthorisedActionException(
                    "Invalid status transition: " + season.getStatus() + " → " + newStatus);
        }
        season.setStatus(newStatus);
        return new SeasonResponse(seasonRepository.save(season));
    }

    @Override
    @Transactional
    public SeasonResponse closeSeason(Long seasonId) {
        Season season = findSeason(seasonId);
        if (season.getStatus() != SeasonStatus.COMPLETED) {
            throw new UnauthorisedActionException(
                    "Season can only be closed when status is COMPLETED, current status: " + season.getStatus());
        }
        season.setStatus(SeasonStatus.CLOSED);
        return new SeasonResponse(seasonRepository.save(season));
    }

    @Override
    @Transactional
    public SeasonResponse addTeamToSeason(Long seasonId, Long teamId) {
        Season season = findSeason(seasonId);
        SeasonGuard.assertNotClosed(season);

        Team team = teamRepository.findByIdAndDeletedFalse(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        SeasonTeamId key = new SeasonTeamId(seasonId, teamId);
        if (seasonTeamRepository.existsById(key)) {
            throw new InvalidSeasonTeamException(
                    "Team '" + team.getTeamName() + "' is already in this season");
        }

        seasonTeamRepository.save(new SeasonTeam(season, team));
        return new SeasonResponse(season);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsInSeason(Long seasonId) {
        findSeason(seasonId); // validate season exists
        return seasonTeamRepository.findBySeasonId(seasonId).stream()
                .map(st -> new TeamResponse(st.getTeam()))
                .toList();
    }

    @Override
    @Transactional
    public void updatePredictionLockTime(Long seasonId, Instant predictionLockTime) {
        Season season = findSeason(seasonId);
        season.setPredictionLockTime(predictionLockTime);
        seasonRepository.save(season);
    }

    @Override
    public Season findSeason(Long seasonId) {
        return seasonRepository.findByIdAndDeletedFalse(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season", seasonId));
    }
}
