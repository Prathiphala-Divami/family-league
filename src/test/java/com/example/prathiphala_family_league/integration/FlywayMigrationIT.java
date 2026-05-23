package com.example.prathiphala_family_league.integration;

import com.example.prathiphala_family_league.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that all Flyway migrations apply cleanly to a fresh database
 * and that V2 seed data (roles, permissions, role_permission) is present.
 */
@SpringBootTest
@ActiveProfiles("it")
class FlywayMigrationIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    // ── V1: all tables exist ───────────────────────────────────────────────────

    @Test
    void allExpectedTablesExist() {
        List<String> expected = List.of(
                "role", "permission", "role_permission", "users",
                "league", "season", "season_team",
                "team", "player",
                "match", "match_result",
                "prediction", "league_prediction",
                "leaderboard", "notification", "audit_log");

        for (String table : expected) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class, table);
            assertThat(count).as("table '%s' must exist", table).isEqualTo(1);
        }
    }

    // ── V2: seed data is present ──────────────────────────────────────────────

    @Test
    void adminAndUserRolesAreSeeded() {
        List<String> roles = jdbc.queryForList("SELECT role_name FROM role", String.class);
        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void allCorePermissionsAreSeeded() {
        List<String> permissions = jdbc.queryForList("SELECT name FROM permission", String.class);
        assertThat(permissions).contains(
                "CREATE_LEAGUE", "CREATE_SEASON", "MANAGE_SEASON_TEAMS",
                "CREATE_MATCH", "PUBLISH_RESULT", "CLOSE_SEASON",
                "MANAGE_USERS", "SEND_NOTIFICATION",
                "SUBMIT_PREDICTION", "VIEW_PREDICTIONS", "VIEW_ALL_PREDICTIONS",
                "MANAGE_TEAMS", "MANAGE_PLAYERS", "VIEW_AUDIT_LOGS");
    }

    @Test
    void adminRoleHasAllAdminPermissions() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM role_permission rp " +
                "JOIN role r ON r.id = rp.role_id " +
                "WHERE r.role_name = 'ADMIN'",
                Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(12);
    }

    @Test
    void userRoleHasPredictionPermissions() {
        List<String> userPerms = jdbc.queryForList(
                "SELECT p.name FROM permission p " +
                "JOIN role_permission rp ON rp.permission_id = p.id " +
                "JOIN role r ON r.id = rp.role_id " +
                "WHERE r.role_name = 'USER'",
                String.class);
        assertThat(userPerms).contains("SUBMIT_PREDICTION", "VIEW_PREDICTIONS");
    }

    // ── V5: audit_log columns and jsonb type ──────────────────────────────────

    @Test
    void auditLogTableHasJsonbColumns() {
        List<String> jsonbCols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                "WHERE table_name = 'audit_log' AND data_type = 'jsonb'",
                String.class);
        assertThat(jsonbCols).containsExactlyInAnyOrder("old_value", "new_value");
    }
}
