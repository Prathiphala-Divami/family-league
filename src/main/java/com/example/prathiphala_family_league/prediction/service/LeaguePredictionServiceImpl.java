package com.example.prathiphala_family_league.prediction.service;

import com.example.prathiphala_family_league.common.exception.InvalidSeasonTeamException;
import com.example.prathiphala_family_league.common.exception.PredictionWindowClosedException;
import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.league.repository.SeasonTeamRepository;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.prediction.dto.LeaguePredictionEntryRequest;
import com.example.prathiphala_family_league.prediction.dto.LeaguePredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitLeaguePredictionRequest;
import com.example.prathiphala_family_league.prediction.entity.LeaguePrediction;
import com.example.prathiphala_family_league.prediction.repository.LeaguePredictionRepository;
import com.example.prathiphala_family_league.team.service.TeamService;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class LeaguePredictionServiceImpl implements LeaguePredictionService {

    private final LeaguePredictionRepository leaguePredictionRepository;
    private final SeasonService seasonService;
    private final SeasonTeamRepository seasonTeamRepository;
    private final TeamService teamService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LeaguePredictionResponse submitOrUpdate(Long userId, Long seasonId,
                                                   SubmitLeaguePredictionRequest request) {
        Season season = seasonService.findSeason(seasonId);

        if (season.getPredictionLockTime() == null || !Instant.now().isBefore(season.getPredictionLockTime())) {
            throw new PredictionWindowClosedException(
                    "League prediction window is closed for season '" + season.getName() + "'");
        }

        Set<Long> enrolledTeamIds = seasonTeamRepository.findTeamIdsBySeasonId(seasonId);
        int expectedCount = enrolledTeamIds.size();
        List<LeaguePredictionEntryRequest> entries = request.getPredictions();

        validateEntries(entries, expectedCount, enrolledTeamIds);

        // Hard-delete previous submission (soft-delete would violate the DB UNIQUE constraints).
        leaguePredictionRepository.deleteByUserIdAndSeasonId(userId, seasonId);

        List<LeaguePrediction> newEntries = entries.stream().map(e -> {
            LeaguePrediction lp = new LeaguePrediction();
            lp.setUser(userRepository.getReferenceById(userId));
            lp.setSeason(season);
            lp.setTeam(teamService.findTeam(e.getTeamId()));
            lp.setPredictedRank(e.getRank());
            return lp;
        }).toList();

        leaguePredictionRepository.saveAll(newEntries);
        return new LeaguePredictionResponse(leaguePredictionRepository.findByUserIdAndSeasonId(userId, seasonId));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaguePredictionResponse getMyPredictions(Long userId, Long seasonId) {
        seasonService.findSeason(seasonId);
        return new LeaguePredictionResponse(
                leaguePredictionRepository.findByUserIdAndSeasonId(userId, seasonId));
    }

    private void validateEntries(List<LeaguePredictionEntryRequest> entries,
                                  int expectedCount, Set<Long> enrolledTeamIds) {
        if (entries.size() != expectedCount) {
            throw new InvalidSeasonTeamException(
                    "Expected exactly " + expectedCount + " team(s) in league prediction, got " + entries.size());
        }

        Set<Long> seenTeams = new HashSet<>();
        Set<Integer> seenRanks = new HashSet<>();

        for (LeaguePredictionEntryRequest e : entries) {
            if (!enrolledTeamIds.contains(e.getTeamId())) {
                throw new InvalidSeasonTeamException("Team " + e.getTeamId() + " is not enrolled in this season");
            }
            if (!seenTeams.add(e.getTeamId())) {
                throw new InvalidSeasonTeamException("Duplicate team " + e.getTeamId() + " in league prediction");
            }
            if (!seenRanks.add(e.getRank())) {
                throw new InvalidSeasonTeamException("Duplicate rank " + e.getRank() + " in league prediction");
            }
        }

        // Ranks must form a contiguous sequence 1..N with no gaps
        List<Integer> sortedRanks = seenRanks.stream().sorted().toList();
        boolean validSequence = IntStream.range(0, sortedRanks.size())
                .allMatch(i -> sortedRanks.get(i) == i + 1);
        if (!validSequence) {
            throw new InvalidSeasonTeamException(
                    "Ranks must form a contiguous sequence 1.." + expectedCount + " with no gaps");
        }
    }
}
