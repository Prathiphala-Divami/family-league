package com.example.prathiphala_family_league.user.dto;

import com.example.prathiphala_family_league.user.entity.User;
import lombok.Getter;

@Getter
public class UserSummaryResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final boolean active;

    public UserSummaryResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().getRoleName();
        this.active = user.isActive();
    }
}
