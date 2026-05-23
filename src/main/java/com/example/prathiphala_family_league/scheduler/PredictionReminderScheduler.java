package com.example.prathiphala_family_league.scheduler;

import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.entity.MatchStatus;
import com.example.prathiphala_family_league.match.repository.MatchRepository;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import com.example.prathiphala_family_league.notification.service.EmailTemplateService;
import com.example.prathiphala_family_league.notification.service.NotificationService;
import com.example.prathiphala_family_league.notification.repository.NotificationRepository;
import com.example.prathiphala_family_league.prediction.repository.PredictionRepository;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionReminderScheduler {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.scheduler.reminder-window-hours:24}")
    private int reminderWindowHours;

    @Scheduled(cron = "${app.scheduler.prediction-reminder-cron}")
    public void sendReminders() {
        log.info("PredictionReminderScheduler started");
        Instant now = Instant.now();
        Instant windowEnd = now.plus(reminderWindowHours, ChronoUnit.HOURS);

        List<Match> upcomingMatches = matchRepository
                .findByPredictionLockTimeBetweenAndStatusAndDeletedFalse(now, windowEnd, MatchStatus.SCHEDULED);

        int sent = 0;
        for (Match match : upcomingMatches) {
            Set<Long> alreadyPredicted = predictionRepository.findUserIdsWithPredictionForMatch(match.getId());
            List<User> activeUsers = userRepository.findAllByDeletedFalseAndActiveTrue();

            String matchDescription = match.getTeam1().getTeamName() + " vs " + match.getTeam2().getTeamName();
            String subject = emailTemplateService.predictionReminderSubject();

            for (User user : activeUsers) {
                if (alreadyPredicted.contains(user.getId())) continue;

                // Idempotency: skip if a SENT reminder already exists for this user + subject
                if (notificationRepository.existsByUserIdAndTypeAndSubjectAndStatus(
                        user.getId(), NotificationType.PREDICTION_REMINDER, subject, NotificationStatus.SENT)) {
                    continue;
                }

                String body = emailTemplateService.predictionReminderBody(
                        user.getName(), matchDescription, match.getPredictionLockTime());
                notificationService.send(user.getId(), NotificationType.PREDICTION_REMINDER, subject, body);
                sent++;
            }
        }

        log.info("PredictionReminderScheduler finished: {} reminder(s) sent", sent);
    }
}
