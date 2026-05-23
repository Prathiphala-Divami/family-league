package com.example.prathiphala_family_league.prediction.service;

import com.example.prathiphala_family_league.match.entity.MatchResult;
import com.example.prathiphala_family_league.match.event.ResultPublishedEvent;
import com.example.prathiphala_family_league.match.repository.MatchResultRepository;
import com.example.prathiphala_family_league.prediction.entity.Prediction;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final MatchResultRepository matchResultRepository;
    private final PredictionRepository predictionRepository;

    // Fires on familyLeagueExecutor after the result-publishing transaction commits.
    // @TransactionalEventListener(AFTER_COMMIT) prevents a race between this read
    // and the writer's transaction; @Async decouples scoring from the HTTP response.
    @Async("familyLeagueExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void calculatePoints(ResultPublishedEvent event) {
        log.info("Scoring started: matchId={} seasonId={}", event.getMatchId(), event.getSeasonId());

        MatchResult result = matchResultRepository.findByMatchIdAndDeletedFalse(event.getMatchId())
                .orElse(null);
        if (result == null) {
            log.warn("Scoring aborted: no result found for matchId={}", event.getMatchId());
            return;
        }

        List<Prediction> predictions = predictionRepository.findMatchPredictions(event.getMatchId());
        predictions.forEach(p -> p.setPointsEarned(score(p, result)));
        predictionRepository.saveAll(predictions);

        log.info("Scoring complete: matchId={} — {} prediction(s) updated", event.getMatchId(), predictions.size());
        // Phase 12 will add: leaderboardService.recalculate(event.getSeasonId())
    }

    private int score(Prediction p, MatchResult r) {
        return switch (r.getResultType()) {
            case WIN      -> scoreWin(p, r);
            case TIE      -> scoreTie(p, r);
            case NO_RESULT -> 0;
        };
    }

    // WIN: correct winner +1, correct toss winner +1, correct POM +1 (max 3 pts)
    private int scoreWin(Prediction p, MatchResult r) {
        int pts = 0;
        if (p.getPredictedWinnerTeam() != null && r.getWinningTeam() != null
                && p.getPredictedWinnerTeam().getId().equals(r.getWinningTeam().getId())) pts++;
        if (p.getPredictedTossWinner() != null && r.getTossWinnerTeam() != null
                && p.getPredictedTossWinner().getId().equals(r.getTossWinnerTeam().getId())) pts++;
        if (p.getPredictedPlayerOfMatch() != null && r.getPlayerOfMatch() != null
                && p.getPredictedPlayerOfMatch().getId().equals(r.getPlayerOfMatch().getId())) pts++;
        return pts;
    }

    // TIE: any predicted winner earns the point (BR-S3), correct toss winner +1 (max 2 pts)
    private int scoreTie(Prediction p, MatchResult r) {
        int pts = 0;
        if (p.getPredictedWinnerTeam() != null) pts++;
        if (p.getPredictedTossWinner() != null && r.getTossWinnerTeam() != null
                && p.getPredictedTossWinner().getId().equals(r.getTossWinnerTeam().getId())) pts++;
        return pts;
    }
}
