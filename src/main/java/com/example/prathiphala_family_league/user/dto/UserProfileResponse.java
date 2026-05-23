package com.example.prathiphala_family_league.user.dto;

import com.example.prathiphala_family_league.user.entity.User;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String avatar;
    private final String role;
    private final boolean active;
    private final Instant createdAt;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.role = user.getRole().getRoleName();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
    }
}
