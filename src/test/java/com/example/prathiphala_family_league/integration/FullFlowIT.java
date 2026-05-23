package com.example.prathiphala_family_league.integration;

import com.example.prathiphala_family_league.AbstractIntegrationTest;
import com.example.prathiphala_family_league.auth.dto.RegisterRequest;
import com.example.prathiphala_family_league.auth.service.AuthService;
import com.example.prathiphala_family_league.leaderboard.entity.Leaderboard;
import com.example.prathiphala_family_league.leaderboard.repository.LeaderboardRepository;
import com.example.prathiphala_family_league.leaderboard.service.LeaderboardService;
import com.example.prathiphala_family_league.league.dto.CreateLeagueRequest;
import com.example.prathiphala_family_league.league.dto.CreateSeasonRequest;
import com.example.prathiphala_family_league.league.dto.LeagueResponse;
import com.example.prathiphala_family_league.league.dto.SeasonResponse;
import com.example.prathiphala_family_league.league.service.LeagueService;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.match.dto.CreateMatchRequest;
import com.example.prathiphala_family_league.match.dto.MatchResponse;
import com.example.prathiphala_family_league.match.dto.PublishResultRequest;
import com.example.prathiphala_family_league.match.entity.ResultType;
import com.example.prathiphala_family_league.match.event.ResultPublishedEvent;
import com.example.prathiphala_family_league.match.service.MatchService;
import com.example.prathiphala_family_league.match.service.ResultService;
import com.example.prathiphala_family_league.prediction.dto.SubmitPredictionRequest;
import com.example.prathiphala_family_league.prediction.entity.Prediction;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.prediction.service.PredictionService;
import com.example.prathiphala_family_league.prediction.service.ScoringService;
import com.example.prathiphala_family_league.team.dto.CreateTeamRequest;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import com.example.prathiphala_family_league.team.service.TeamService;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end service-layer flow:
 *   register users → create league/season/teams/match
 *   → submit predictions → publish result
 *   → score → verify leaderboard
 *
 * ScoringService.calculatePoints() is called directly to bypass @Async
 * (async dispatch only fires in a real request context).
 */
@SpringBootTest
@ActiveProfiles("it")
class FullFlowIT extends AbstractIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private LeagueService leagueService;
    @Autowired private SeasonService seasonService;
    @Autowired private TeamService teamService;
    @Autowired private MatchService matchService;
    @Autowired private PredictionService predictionService;
    @Autowired private ResultService resultService;
    @Autowired private ScoringService scoringService;
    @Autowired private LeaderboardService leaderboardService;
    @Autowired private PredictionRepository predictionRepository;
    @Autowired private LeaderboardRepository leaderboardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDomainData() {
        // Remove domain rows only — leave role/permission seed data intact.
        jdbc.execute("TRUNCATE TABLE audit_log, notification, leaderboard, " +
                     "league_prediction, prediction, match_result, match, " +
                     "season_team, season, league, player, team, users CASCADE");
    }

    @Test
    void matchPredictionResultScoring_correctPointsAndLeaderboard() {

        // ── 1. Create two users ────────────────────────────────────────────────
        authService.register(reg("Alice", "alice@test.com", "password123"));
        authService.register(reg("Bob",   "bob@test.com",   "password123"));

        User alice = userRepository.findByEmailAndDeletedFalse("alice@test.com").orElseThrow();
        User bob   = userRepository.findByEmailAndDeletedFalse("bob@test.com").orElseThrow();

        // ── 2. Create league, season, two teams ────────────────────────────────
        LeagueResponse league = leagueService.create(league("IPL 2025"));
        SeasonResponse season = seasonService.create(league.getId(), season("Season 1"));

        TeamResponse teamA = teamService.create(team("Chennai", "CSK"));
        TeamResponse teamB = teamService.create(team("Mumbai",  "MI"));

        // ── 3. Add teams to season ─────────────────────────────────────────────
        seasonService.addTeamToSeason(season.getId(), teamA.getId());
        seasonService.addTeamToSeason(season.getId(), teamB.getId());

        // ── 4. Schedule a match (start time 2 hours from now) ─────────────────
        CreateMatchRequest matchReq = new CreateMatchRequest();
        ReflectionTestUtils.setField(matchReq, "team1Id",    teamA.getId());
        ReflectionTestUtils.setField(matchReq, "team2Id",    teamB.getId());
        ReflectionTestUtils.setField(matchReq, "startTime",  Instant.now().plus(2, ChronoUnit.HOURS));
        ReflectionTestUtils.setField(matchReq, "matchNumber", 1);

        MatchResponse match = matchService.create(season.getId(), matchReq);

        // ── 5. Submit predictions ──────────────────────────────────────────────
        // Alice: correct winner (CSK) + correct toss (MI) → 2 pts (no POM prediction)
        SubmitPredictionRequest aliceReq = new SubmitPredictionRequest();
        ReflectionTestUtils.setField(aliceReq, "predictedWinnerTeamId", teamA.getId());
        ReflectionTestUtils.setField(aliceReq, "predictedTossWinnerId", teamB.getId());
        predictionService.submitOrUpdate(alice.getId(), match.getId(), aliceReq);

        // Bob: wrong winner (MI loses), correct toss (MI) → 1 pt
        SubmitPredictionRequest bobReq = new SubmitPredictionRequest();
        ReflectionTestUtils.setField(bobReq, "predictedWinnerTeamId", teamB.getId()); // wrong
        ReflectionTestUtils.setField(bobReq, "predictedTossWinnerId", teamB.getId()); // correct
        predictionService.submitOrUpdate(bob.getId(), match.getId(), bobReq);

        // ── 6. Publish result: CSK wins, MI won toss ───────────────────────────
        PublishResultRequest resultReq = new PublishResultRequest();
        ReflectionTestUtils.setField(resultReq, "resultType",       ResultType.WIN);
        ReflectionTestUtils.setField(resultReq, "winningTeamId",    teamA.getId());   // CSK wins
        ReflectionTestUtils.setField(resultReq, "tossWinnerTeamId", teamB.getId());   // MI won toss
        ReflectionTestUtils.setField(resultReq, "playerOfMatchId",  null);

        resultService.publishResult(match.getId(), resultReq, alice.getId());

        // ── 7. Score — call directly to bypass @Async ─────────────────────────
        scoringService.calculatePoints(new ResultPublishedEvent(match.getId(), season.getId()));

        // ── 8. Assert pointsEarned ─────────────────────────────────────────────
        List<Prediction> predictions = predictionRepository.findMatchPredictions(match.getId());
        assertThat(predictions).hasSize(2);

        Prediction alicePred = predictions.stream()
                .filter(p -> p.getUser().getId().equals(alice.getId()))
                .findFirst().orElseThrow();
        Prediction bobPred = predictions.stream()
                .filter(p -> p.getUser().getId().equals(bob.getId()))
                .findFirst().orElseThrow();

        assertThat(alicePred.getPointsEarned()).isEqualTo(2); // winner + toss
        assertThat(bobPred.getPointsEarned()).isEqualTo(1);   // toss only

        // ── 9. Assert leaderboard ──────────────────────────────────────────────
        leaderboardService.recalculate(season.getId());

        List<Leaderboard> board = leaderboardRepository
                .findBySeasonIdOrderByTotalPointsDesc(season.getId());
        assertThat(board).hasSize(2);

        Leaderboard aliceEntry = board.stream()
                .filter(e -> e.getUserId().equals(alice.getId()))
                .findFirst().orElseThrow();
        Leaderboard bobEntry = board.stream()
                .filter(e -> e.getUserId().equals(bob.getId()))
                .findFirst().orElseThrow();

        assertThat(aliceEntry.getTotalPoints()).isEqualTo(2);
        assertThat(aliceEntry.getRankPosition()).isEqualTo(1);
        assertThat(bobEntry.getTotalPoints()).isEqualTo(1);
        assertThat(bobEntry.getRankPosition()).isEqualTo(2);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RegisterRequest reg(String name, String email, String password) {
        RegisterRequest r = new RegisterRequest();
        ReflectionTestUtils.setField(r, "name",     name);
        ReflectionTestUtils.setField(r, "email",    email);
        ReflectionTestUtils.setField(r, "password", password);
        return r;
    }

    private CreateLeagueRequest league(String name) {
        CreateLeagueRequest r = new CreateLeagueRequest();
        ReflectionTestUtils.setField(r, "name", name);
        return r;
    }

    private CreateSeasonRequest season(String name) {
        CreateSeasonRequest r = new CreateSeasonRequest();
        ReflectionTestUtils.setField(r, "name",      name);
        ReflectionTestUtils.setField(r, "startDate", LocalDate.now());
        ReflectionTestUtils.setField(r, "endDate",   LocalDate.now().plusMonths(2));
        return r;
    }

    private CreateTeamRequest team(String name, String shortName) {
        CreateTeamRequest r = new CreateTeamRequest();
        ReflectionTestUtils.setField(r, "teamName",  name);
        ReflectionTestUtils.setField(r, "shortName", shortName);
        return r;
    }
}
