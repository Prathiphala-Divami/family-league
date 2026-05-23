package com.example.prathiphala_family_league.notification.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    public String predictionReminderSubject() {
        return "Reminder: Submit your match prediction before the window closes!";
    }

    public String predictionReminderBody(String userName, String matchDescription, Instant lockTime) {
        return "Hi " + userName + ",\n\n"
                + "The prediction window for " + matchDescription + " closes at " + FMT.format(lockTime) + ".\n"
                + "Don't forget to submit your prediction!\n\n"
                + "Family League Team";
    }

    public String resultAlertSubject(String matchDescription) {
        return "Action Required: Publish result for " + matchDescription;
    }

    public String resultAlertBody(String matchDescription) {
        return "Match '" + matchDescription + "' has concluded.\n"
                + "Please publish the result in the Family League platform.";
    }

    public String leaderboardUpdateSubject(String seasonName) {
        return "Leaderboard Updated – Season: " + seasonName;
    }

    public String leaderboardUpdateBody(String seasonName) {
        return "The leaderboard for season '" + seasonName + "' has been recalculated.\n"
                + "Check the latest standings in the Family League platform.";
    }
}
