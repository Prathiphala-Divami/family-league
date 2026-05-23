package com.example.prathiphala_family_league.scheduler;

import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.repository.MatchRepository;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import com.example.prathiphala_family_league.notification.repository.NotificationRepository;
import com.example.prathiphala_family_league.notification.service.EmailTemplateService;
import com.example.prathiphala_family_league.notification.service.NotificationService;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultAlertScheduler {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Scheduled(cron = "${app.scheduler.result-alert-cron}")
    public void alertAdmin() {
        log.info("ResultAlertScheduler started");

        List<Match> unresolved = matchRepository.findCompletedMatchesWithoutResult();
        int sent = 0;

        for (Match match : unresolved) {
            String matchDescription = match.getTeam1().getTeamName() + " vs " + match.getTeam2().getTeamName();
            String subject = emailTemplateService.resultAlertSubject(matchDescription);

            List<User> admins = userRepository.findActiveUsersByRoleName("ADMIN");
            for (User admin : admins) {

                // Idempotency: skip if alert already sent to this admin for this match
                if (notificationRepository.existsByUserIdAndTypeAndSubjectAndStatus(
                        admin.getId(), NotificationType.RESULT_PUBLISHED, subject, NotificationStatus.SENT)) {
                    continue;
                }

                String body = emailTemplateService.resultAlertBody(matchDescription);
                notificationService.send(admin.getId(), NotificationType.RESULT_PUBLISHED, subject, body);
                sent++;
            }
        }

        log.info("ResultAlertScheduler finished: {} alert(s) sent", sent);
    }
}
