package com.example.prathiphala_family_league.audit.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditSerializer {

    private final ObjectMapper mapper;

    public AuditSerializer() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.addMixIn(Object.class, PasswordExclusionMixin.class);
    }

    public String serialize(Object entity) {
        if (entity == null) return null;
        try {
            return mapper.writeValueAsString(entity);
        } catch (Exception e) {
            log.warn("Audit serialization failed for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
            return "{\"error\":\"serialization_failed\"}";
        }
    }

    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    abstract static class PasswordExclusionMixin {}
}
