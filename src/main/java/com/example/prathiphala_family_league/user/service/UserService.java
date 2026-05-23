package com.example.prathiphala_family_league.user.service;

import com.example.prathiphala_family_league.user.dto.UpdateProfileRequest;
import com.example.prathiphala_family_league.user.dto.UserProfileResponse;
import com.example.prathiphala_family_league.user.dto.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse getMyProfile(Long userId);
    UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request);
    Page<UserSummaryResponse> getAllUsers(Pageable pageable, String search);
    void updateUserStatus(Long targetUserId, boolean active, Long currentUserId);
}
