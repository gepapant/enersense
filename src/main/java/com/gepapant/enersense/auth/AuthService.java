package com.gepapant.enersense.auth;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gepapant.enersense.user.AppUser;
import com.gepapant.enersense.user.AppUserRepository;
import com.gepapant.enersense.user.Role;
import com.gepapant.enersense.user.UserResponse;

@Service
public class AuthService {

    private final AppUserRepository
        userRepository;

    private final PasswordEncoder
        passwordEncoder;

    private final JwtTokenService
        jwtTokenService;

    public AuthService(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.userRepository =
            userRepository;

        this.passwordEncoder =
            passwordEncoder;

        this.jwtTokenService =
            jwtTokenService;
    }

    @Transactional
    public AuthResponse bootstrap(
        BootstrapRequest request
    ) {

        if (
            userRepository.count() > 0
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "EnerSense already has users"
            );
        }

        String email =
            normalizeEmail(
                request.email()
            );

        String passwordHash =
            passwordEncoder.encode(
                request.password()
            );

        AppUser admin =
            new AppUser(
                email,
                passwordHash,
                Role.ADMIN
            );

        AppUser saved =
            userRepository.save(
                admin
            );

        return createAuthResponse(
            saved
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(
        LoginRequest request
    ) {

        String email =
            normalizeEmail(
                request.email()
            );

        AppUser user =
            userRepository
                .findByEmailIgnoreCase(
                    email
                )
                .orElseThrow(
                    () ->
                        unauthorized()
                );

        if (
            !user.isActive()
            ||
            !passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
            )
        ) {

            throw unauthorized();
        }

        return createAuthResponse(
            user
        );
    }

    @Transactional(readOnly = true)
    public UserResponse me(
        String email
    ) {

        AppUser user =
            userRepository
                .findByEmailIgnoreCase(
                    email
                )
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                        )
                );

        return UserResponse.from(
            user
        );
    }

    private AuthResponse
        createAuthResponse(
            AppUser user
        ) {

        Jwt jwt =
            jwtTokenService.issueToken(
                user
            );

        return new AuthResponse(
            jwt.getTokenValue(),
            jwt.getExpiresAt(),
            UserResponse.from(user)
        );
    }

    private ResponseStatusException
        unauthorized() {

        return new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );
    }

    private String normalizeEmail(
        String email
    ) {

        return email
            .trim()
            .toLowerCase(
                Locale.ROOT
            );
    }
}