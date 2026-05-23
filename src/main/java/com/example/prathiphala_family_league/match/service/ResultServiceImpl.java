package com.example.prathiphala_family_league.match.service;

import com.example.prathiphala_family_league.common.exception.DuplicateResultException;
import com.example.prathiphala_family_league.common.exception.InvalidResultException;
import com.example.prathiphala_family_league.match.dto.MatchResultResponse;
import com.example.prathiphala_family_league.match.dto.PublishResultRequest;
import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.entity.MatchResult;
import com.example.prathiphala_family_league.match.entity.ResultType;
import com.example.prathiphala_family_league.match.event.ResultPublishedEvent;
import com.example.prathiphala_family_league.match.repository.MatchResultRepository;
import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.service.PlayerService;
import com.example.prathiphala_family_league.team.service.TeamService;
import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final MatchResultRepository matchResultRepository;
    private final MatchService matchService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MatchResultResponse publishResult(Long matchId, PublishResultRequest request, Long publisherId) {
        Match match = matchService.findMatch(matchId);

        if (matchResultRepository.existsByMatchId(matchId)) {
            throw new DuplicateResultException("A result has already been published for match " + matchId);
        }

        validatePayload(request, match);

        Team winningTeam = request.getWinningTeamId() != null
                ? teamService.findTeam(request.getWinningTeamId()) : null;
        Team tossWinner = request.getTossWinnerTeamId() != null
                ? teamService.findTeam(request.getTossWinnerTeamId()) : null;
        Player playerOfMatch = request.getPlayerOfMatchId() != null
                ? playerService.findPlayer(request.getPlayerOfMatchId()) : null;

        MatchResult result = new MatchResult();
        result.setMatch(match);
        result.setResultType(request.getResultType());
        result.setWinningTeam(winningTeam);
        result.setTossWinnerTeam(tossWinner);
        result.setPlayerOfMatch(playerOfMatch);
        result.setWinningMargin(request.getWinningMargin());
        result.setPublishedBy(userRepository.getReferenceById(publisherId));
        result.setPublishedAt(Instant.now());

        MatchResult saved = matchResultRepository.save(result);

        // Mark match as COMPLETED and fire async scoring event.
        matchService.markCompleted(matchId);
        eventPublisher.publishEvent(new ResultPublishedEvent(matchId, match.getSeason().getId()));

        return new MatchResultResponse(matchResultRepository.findByMatchIdAndDeletedFalse(matchId)
                .orElse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResultResponse getResult(Long matchId) {
        matchService.findMatch(matchId);
        return matchResultRepository.findByMatchIdAndDeletedFalse(matchId)
                .map(MatchResultResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Result for match", matchId));
    }

    private void validatePayload(PublishResultRequest req, Match match) {
        if (req.getResultType() == ResultType.WIN) {
            if (req.getWinningTeamId() == null) {
                throw new InvalidResultException("winningTeamId is required for a WIN result");
            }
            if (req.getPlayerOfMatchId() == null) {
                throw new InvalidResultException("playerOfMatchId is required for a WIN result");
            }
            Long wid = req.getWinningTeamId();
            boolean inMatch = wid.equals(match.getTeam1().getId()) || wid.equals(match.getTeam2().getId());
            if (!inMatch) {
                throw new InvalidResultException("winningTeamId does not match either team in this match");
            }
        } else {
            if (req.getWinningTeamId() != null) {
                throw new InvalidResultException(
                        "winningTeamId must be null for a " + req.getResultType() + " result");
            }
        }
    }
}
