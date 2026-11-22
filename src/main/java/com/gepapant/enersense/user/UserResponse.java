package com.gepapant.enersense.user;

import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    Role role,
    boolean active,
    Instant createdAt
) {

    public static UserResponse from(
        AppUser user
    ) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt()
        );
    }
}