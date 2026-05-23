package com.example.prathiphala_family_league;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for all integration tests. Spins up one shared PostgreSQL container per JVM via
 * the static field — Testcontainers reuses it across test classes in the same process.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by Testcontainers JUnit 5 extension
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("familyleague_test")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Let each test class choose its Flyway + DDL strategy via @ActiveProfiles
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-min-32-chars!!");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "3025");
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("app.notification.from-address", () -> "noreply@test.local");
        registry.add("app.notification.admin-email",  () -> "admin@test.local");
    }
}
