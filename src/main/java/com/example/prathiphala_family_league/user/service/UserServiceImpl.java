package com.example.prathiphala_family_league.user.service;

import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.common.exception.UnauthorisedActionException;
import com.example.prathiphala_family_league.user.dto.UpdateProfileRequest;
import com.example.prathiphala_family_league.user.dto.UserProfileResponse;
import com.example.prathiphala_family_league.user.dto.UserSummaryResponse;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        return new UserProfileResponse(findActiveUser(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        if (!StringUtils.hasText(request.getName()) && !StringUtils.hasText(request.getAvatar())) {
            throw new UnauthorisedActionException("At least one of name or avatar must be provided");
        }
        User user = findActiveUser(userId);
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        return new UserProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getAllUsers(Pageable pageable, String search) {
        String trimmed = StringUtils.hasText(search) ? search.trim() : null;
        return userRepository.searchByNameOrEmail(trimmed, pageable)
                .map(UserSummaryResponse::new);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long targetUserId, boolean active, Long currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new UnauthorisedActionException("You cannot change your own active status");
        }
        User user = findActiveUser(targetUserId);
        user.setActive(active);
        userRepository.save(user);
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
