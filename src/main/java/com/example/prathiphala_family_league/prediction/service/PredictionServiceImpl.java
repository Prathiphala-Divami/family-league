package com.example.prathiphala_family_league.prediction.service;

import com.example.prathiphala_family_league.common.exception.InvalidSeasonTeamException;
import com.example.prathiphala_family_league.common.exception.PredictionWindowClosedException;
import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.service.MatchService;
import com.example.prathiphala_family_league.prediction.dto.PredictionResponse;
import com.example.prathiphala_family_league.prediction.dto.SubmitPredictionRequest;
import com.example.prathiphala_family_league.prediction.entity.Prediction;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.service.PlayerService;
import com.example.prathiphala_family_league.team.service.TeamService;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchService matchService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PredictionResponse submitOrUpdate(Long userId, Long matchId, SubmitPredictionRequest request) {
        Match match = matchService.findMatch(matchId);

        if (!Instant.now().isBefore(match.getPredictionLockTime())) {
            throw new PredictionWindowClosedException(
                    "Prediction window closed at " + match.getPredictionLockTime());
        }

        // Validate optional predicted teams belong to this match
        Team predictedWinner = resolveMatchTeam(request.getPredictedWinnerTeamId(), match, "predictedWinnerTeamId");
        Team predictedTossWinner = resolveMatchTeam(request.getPredictedTossWinnerId(), match, "predictedTossWinnerId");
        Player predictedPOM = resolveMatchPlayer(request.getPredictedPlayerOfMatchId(), match);

        Prediction prediction = predictionRepository.findByMatchIdAndUserId(matchId, userId)
                .orElseGet(() -> {
                    Prediction p = new Prediction();
                    p.setUser(userRepository.getReferenceById(userId));
                    p.setMatch(match);
                    return p;
                });

        // Restore if previously soft-deleted (should not happen in normal flow)
        prediction.setDeleted(false);
        prediction.setDeletedAt(null);

        prediction.setPredictedWinnerTeam(predictedWinner);
        prediction.setPredictedTossWinner(predictedTossWinner);
        prediction.setPredictedPlayerOfMatch(predictedPOM);
        prediction.setSubmittedAt(Instant.now());

        return new PredictionResponse(predictionRepository.save(prediction));
    }

    @Override
    @Transactional(readOnly = true)
    public PredictionResponse getMyPrediction(Long userId, Long matchId) {
        matchService.findMatch(matchId);
        return predictionRepository.findMyPrediction(matchId, userId)
                .map(PredictionResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Prediction for match " + matchId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionResponse> getAllPredictions(Long matchId, Long callerUserId) {
        Match match = matchService.findMatch(matchId);
        List<Prediction> all = predictionRepository.findMatchPredictions(matchId);

        // Before lock: each user sees only their own prediction
        if (Instant.now().isBefore(match.getPredictionLockTime())) {
            return all.stream()
                    .filter(p -> p.getUser().getId().equals(callerUserId))
                    .map(PredictionResponse::new)
                    .toList();
        }
        return all.stream().map(PredictionResponse::new).toList();
    }

    // Returns null if teamId is null; validates team belongs to match if provided.
    private Team resolveMatchTeam(Long teamId, Match match, String field) {
        if (teamId == null) return null;
        Team team = teamService.findTeam(teamId);
        boolean inMatch = team.getId().equals(match.getTeam1().getId())
                || team.getId().equals(match.getTeam2().getId());
        if (!inMatch) {
            throw new InvalidSeasonTeamException(
                    field + " team '" + team.getTeamName() + "' is not part of this match");
        }
        return team;
    }

    // Returns null if playerId is null; validates player belongs to one of the match teams.
    private Player resolveMatchPlayer(Long playerId, Match match) {
        if (playerId == null) return null;
        Player player = playerService.findPlayer(playerId);
        Long playerTeamId = player.getTeam().getId();
        boolean inMatch = playerTeamId.equals(match.getTeam1().getId())
                || playerTeamId.equals(match.getTeam2().getId());
        if (!inMatch) {
            throw new InvalidSeasonTeamException(
                    "Player '" + player.getPlayerName() + "' does not belong to either team in this match");
        }
        return player;
    }
}
