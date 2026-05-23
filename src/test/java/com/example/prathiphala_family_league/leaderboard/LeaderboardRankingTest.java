package com.example.prathiphala_family_league.leaderboard;

import com.example.prathiphala_family_league.leaderboard.entity.Leaderboard;
import com.example.prathiphala_family_league.leaderboard.repository.LeaderboardRepository;
import com.example.prathiphala_family_league.leaderboard.service.LeaderboardServiceImpl;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.notification.service.EmailTemplateService;
import com.example.prathiphala_family_league.notification.service.NotificationService;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.prediction.repository.UserPointsProjection;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardRankingTest {

    @Mock private LeaderboardRepository leaderboardRepository;
    @Mock private PredictionRepository predictionRepository;
    @Mock private SeasonService seasonService;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailTemplateService emailTemplateService;

    private LeaderboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LeaderboardServiceImpl(
                leaderboardRepository, predictionRepository, seasonService,
                userRepository, notificationService, emailTemplateService);
    }

    // ── AC-3: 8 pts → rank 1; 5 pts + 5 pts → rank 2 + rank 2 ──────────────

    @Test
    void denseRank_topScorerRank1_tiedUsersShareRank2() {
        Long seasonId = 1L;

        when(predictionRepository.sumPointsByUserForSeason(seasonId)).thenReturn(List.of(
                pts(1L, 8), pts(2L, 5), pts(3L, 5)));

        List<Leaderboard> storedEntries = List.of(
                entry(1L, 8), entry(2L, 5), entry(3L, 5));
        when(leaderboardRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId)).thenReturn(storedEntries);
        when(userRepository.findActiveUsersByRoleName("ADMIN")).thenReturn(List.of());

        service.recalculate(seasonId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Leaderboard>> captor = ArgumentCaptor.forClass(List.class);
        verify(leaderboardRepository).saveAll(captor.capture());

        List<Leaderboard> saved = captor.getValue();
        assertThat(rankOf(saved, 1L)).isEqualTo(1);   // 8 pts → rank 1
        assertThat(rankOf(saved, 2L)).isEqualTo(2);   // 5 pts → rank 2
        assertThat(rankOf(saved, 3L)).isEqualTo(2);   // 5 pts → rank 2 (same as user 2)
    }

    @Test
    void denseRank_allSamePoints_allRank1() {
        Long seasonId = 2L;

        when(predictionRepository.sumPointsByUserForSeason(seasonId)).thenReturn(List.of(
                pts(1L, 3), pts(2L, 3), pts(3L, 3)));

        List<Leaderboard> storedEntries = List.of(
                entry(1L, 3), entry(2L, 3), entry(3L, 3));
        when(leaderboardRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId)).thenReturn(storedEntries);
        when(userRepository.findActiveUsersByRoleName("ADMIN")).thenReturn(List.of());

        service.recalculate(seasonId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Leaderboard>> captor = ArgumentCaptor.forClass(List.class);
        verify(leaderboardRepository).saveAll(captor.capture());

        List<Leaderboard> saved = captor.getValue();
        assertThat(saved).allMatch(e -> e.getRankPosition() == 1);
    }

    @Test
    void denseRank_strictlyDescending_noSharedRanks() {
        Long seasonId = 3L;

        when(predictionRepository.sumPointsByUserForSeason(seasonId)).thenReturn(List.of(
                pts(1L, 10), pts(2L, 7), pts(3L, 4)));

        List<Leaderboard> storedEntries = List.of(
                entry(1L, 10), entry(2L, 7), entry(3L, 4));
        when(leaderboardRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId)).thenReturn(storedEntries);
        when(userRepository.findActiveUsersByRoleName("ADMIN")).thenReturn(List.of());

        service.recalculate(seasonId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Leaderboard>> captor = ArgumentCaptor.forClass(List.class);
        verify(leaderboardRepository).saveAll(captor.capture());

        List<Leaderboard> saved = captor.getValue();
        assertThat(rankOf(saved, 1L)).isEqualTo(1);
        assertThat(rankOf(saved, 2L)).isEqualTo(2);
        assertThat(rankOf(saved, 3L)).isEqualTo(3);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserPointsProjection pts(Long userId, int points) {
        return new UserPointsProjection() {
            public Long getUserId()      { return userId; }
            public Integer getTotalPoints() { return points; }
        };
    }

    private Leaderboard entry(Long userId, int points) {
        Leaderboard e = new Leaderboard();
        e.setUserId(userId);
        e.setTotalPoints(points);
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private int rankOf(List<Leaderboard> entries, Long userId) {
        return entries.stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .map(Leaderboard::getRankPosition)
                .orElseThrow();
    }
}
