package com.example.prathiphala_family_league.auth.service;

import com.example.prathiphala_family_league.auth.dto.AuthResponse;
import com.example.prathiphala_family_league.auth.dto.LoginRequest;
import com.example.prathiphala_family_league.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
