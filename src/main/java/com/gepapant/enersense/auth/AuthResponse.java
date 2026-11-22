package com.gepapant.enersense.auth;

import java.time.Instant;

import com.gepapant.enersense.user.UserResponse;

public record AuthResponse(
    String token,
    Instant expiresAt,
    UserResponse user
) {
}