package com.example.prathiphala_family_league.prediction;

import com.example.prathiphala_family_league.leaderboard.service.LeaderboardService;
import com.example.prathiphala_family_league.match.entity.MatchResult;
import com.example.prathiphala_family_league.match.entity.ResultType;
import com.example.prathiphala_family_league.match.event.ResultPublishedEvent;
import com.example.prathiphala_family_league.match.repository.MatchResultRepository;
import com.example.prathiphala_family_league.prediction.entity.Prediction;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.prediction.service.ScoringService;
import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock private MatchResultRepository matchResultRepository;
    @Mock private PredictionRepository predictionRepository;
    @Mock private LeaderboardService leaderboardService;

    private ScoringService scoringService;

    private Team teamA;
    private Team teamB;
    private Player pom;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService(matchResultRepository, predictionRepository, leaderboardService);

        teamA = new Team(); teamA.setId(1L);
        teamB = new Team(); teamB.setId(2L);
        pom   = new Player(); pom.setId(10L);
    }

    // ── WIN scoring (max 3 pts) ───────────────────────────────────────────────

    @Test
    void win_allThreeCorrect_returns3() {
        MatchResult result = winResult(teamA, teamB, pom);
        Prediction prediction = prediction(teamA, teamB, pom);

        assertPoints(result, prediction, 3);
    }

    @Test
    void win_onlyWinnerCorrect_returns1() {
        MatchResult result = winResult(teamA, teamB, pom);
        Prediction prediction = prediction(teamA, null, null);  // only winner correct

        assertPoints(result, prediction, 1);
    }

    @Test
    void win_onlyTossCorrect_returns1() {
        MatchResult result = winResult(teamA, teamB, pom);
        Prediction prediction = prediction(teamB, teamB, null); // wrong winner, correct toss

        assertPoints(result, prediction, 1);
    }

    @Test
    void win_onlyPomCorrect_returns1() {
        MatchResult result = winResult(teamA, teamB, pom);
        Prediction prediction = prediction(teamB, teamA, pom); // wrong winner+toss, correct POM

        assertPoints(result, prediction, 1);
    }

    @Test
    void win_nothingCorrect_returns0() {
        MatchResult result = winResult(teamA, teamB, pom);
        Prediction prediction = prediction(teamB, teamA, null); // wrong winner, wrong toss, no POM

        assertPoints(result, prediction, 0);
    }

    // ── TIE scoring (max 2 pts) ───────────────────────────────────────────────

    @Test
    void tie_anyWinnerPredicted_earnsTossPoint_returns2() {
        MatchResult result = tieResult(teamB);    // toss winner = teamB
        Prediction prediction = prediction(teamA, teamB, null); // any team predicted → +1, toss correct → +1

        assertPoints(result, prediction, 2);
    }

    @Test
    void tie_anyWinnerPredictedButWrongToss_returns1() {
        MatchResult result = tieResult(teamB);
        Prediction prediction = prediction(teamA, teamA, null); // any team → +1, toss wrong → 0

        assertPoints(result, prediction, 1);
    }

    @Test
    void tie_noWinnerPredicted_returns0() {
        MatchResult result = tieResult(teamB);
        Prediction prediction = prediction(null, teamB, null); // no winner → 0, toss correct → +1 (only toss)

        assertPoints(result, prediction, 1); // toss still counts even without winner
    }

    // ── NO_RESULT ─────────────────────────────────────────────────────────────

    @Test
    void noResult_allPredicted_returns0() {
        MatchResult result = new MatchResult();
        result.setResultType(ResultType.NO_RESULT);

        Prediction prediction = prediction(teamA, teamB, pom); // everything predicted, but no result → 0

        assertPoints(result, prediction, 0);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void assertPoints(MatchResult result, Prediction p, int expected) {
        Long matchId = 99L;
        ResultPublishedEvent event = new ResultPublishedEvent(matchId, 1L);

        when(matchResultRepository.findByMatchIdAndDeletedFalse(matchId)).thenReturn(Optional.of(result));
        when(predictionRepository.findMatchPredictions(matchId)).thenReturn(List.of(p));

        scoringService.calculatePoints(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Prediction>> captor = ArgumentCaptor.forClass(List.class);
        verify(predictionRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getPointsEarned()).isEqualTo(expected);
    }

    private MatchResult winResult(Team winner, Team tossWinner, Player player) {
        MatchResult r = new MatchResult();
        r.setResultType(ResultType.WIN);
        r.setWinningTeam(winner);
        r.setTossWinnerTeam(tossWinner);
        r.setPlayerOfMatch(player);
        return r;
    }

    private MatchResult tieResult(Team tossWinner) {
        MatchResult r = new MatchResult();
        r.setResultType(ResultType.TIE);
        r.setTossWinnerTeam(tossWinner);
        return r;
    }

    private Prediction prediction(Team winner, Team toss, Player player) {
        Prediction p = new Prediction();
        p.setPredictedWinnerTeam(winner);
        p.setPredictedTossWinner(toss);
        p.setPredictedPlayerOfMatch(player);
        return p;
    }
}
