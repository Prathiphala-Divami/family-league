package com.example.prathiphala_family_league.auth.service;

import com.example.prathiphala_family_league.auth.dto.AuthResponse;
import com.example.prathiphala_family_league.auth.dto.LoginRequest;
import com.example.prathiphala_family_league.auth.dto.RegisterRequest;
import com.example.prathiphala_family_league.auth.util.JwtUtil;
import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.common.exception.UnauthorisedActionException;
import com.example.prathiphala_family_league.user.entity.Role;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.RoleRepository;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmailAndDeletedFalse(request.getEmail()).isPresent()) {
            throw new UnauthorisedActionException("Email address is already registered");
        }

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role 'USER' not found — ensure V2 seed migration has run"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorisedActionException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorisedActionException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> permissions = user.getRole().getPermissions().stream()
                .map(p -> p.getName())
                .toList();

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().getRoleName(),
                permissions
        );

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirySeconds())
                .build();
    }
}
