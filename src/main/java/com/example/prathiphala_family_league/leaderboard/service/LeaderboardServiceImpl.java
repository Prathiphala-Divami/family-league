package com.example.prathiphala_family_league.leaderboard.service;

import com.example.prathiphala_family_league.leaderboard.dto.LeaderboardEntryResponse;
import com.example.prathiphala_family_league.leaderboard.entity.Leaderboard;
import com.example.prathiphala_family_league.leaderboard.repository.LeaderboardRepository;
import com.example.prathiphala_family_league.league.service.SeasonService;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import com.example.prathiphala_family_league.notification.service.EmailTemplateService;
import com.example.prathiphala_family_league.notification.service.NotificationService;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.prediction.repository.UserPointsProjection;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final PredictionRepository predictionRepository;
    private final SeasonService seasonService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Override
    @Transactional
    public void recalculate(Long seasonId) {
        List<UserPointsProjection> scores = predictionRepository.sumPointsByUserForSeason(seasonId);

        // Upsert one row per user (PostgreSQL ON CONFLICT DO UPDATE)
        for (UserPointsProjection score : scores) {
            leaderboardRepository.upsertTotalPoints(seasonId, score.getUserId(), score.getTotalPoints());
        }

        // Load all rows, compute DENSE_RANK in Java, batch-save
        List<Leaderboard> entries = leaderboardRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId);
        assignDenseRanks(entries);
        entries.forEach(e -> e.setUpdatedAt(Instant.now()));
        leaderboardRepository.saveAll(entries);

        log.info("Leaderboard recalculation complete for seasonId={} ({} entries)", seasonId, entries.size());

        notifyAdmin(seasonId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> getLeaderboard(Long seasonId, Pageable pageable) {
        seasonService.findSeason(seasonId);
        Page<Leaderboard> page = leaderboardRepository.findBySeasonIdOrderByRankPositionAsc(seasonId, pageable);

        Set<Long> userIds = page.stream().map(Leaderboard::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return page.map(e -> new LeaderboardEntryResponse(
                e.getRankPosition(),
                e.getUserId(),
                userNames.getOrDefault(e.getUserId(), "Unknown"),
                e.getTotalPoints()));
    }

    // DENSE_RANK: tied scores share a rank; the next distinct score gets the next integer rank.
    private void assignDenseRanks(List<Leaderboard> sortedDesc) {
        int denseRank = 0;
        int prevPoints = Integer.MAX_VALUE;
        for (Leaderboard entry : sortedDesc) {
            if (entry.getTotalPoints() < prevPoints) {
                denseRank++;
                prevPoints = entry.getTotalPoints();
            }
            entry.setRankPosition(denseRank);
        }
    }

    private void notifyAdmin(Long seasonId) {
        try {
            String seasonName = seasonService.findSeason(seasonId).getName();
            userRepository.findActiveUsersByRoleName("ADMIN").forEach(admin ->
                    notificationService.send(
                            admin.getId(),
                            NotificationType.LEADERBOARD_UPDATE,
                            emailTemplateService.leaderboardUpdateSubject(seasonName),
                            emailTemplateService.leaderboardUpdateBody(seasonName)));
        } catch (Exception ex) {
            log.warn("Failed to send leaderboard update notification for seasonId={}: {}", seasonId, ex.getMessage());
        }
    }
}
