package com.gepapant.enersense.user;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final AppUserRepository
        userRepository;

    private final PasswordEncoder
        passwordEncoder;

    public UserService(
        AppUserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository =
            userRepository;

        this.passwordEncoder =
            passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {

        return userRepository
            .findAll()
            .stream()
            .sorted(
                Comparator.comparing(
                    AppUser::getId
                )
            )
            .map(
                UserResponse::from
            )
            .toList();
    }

    @Transactional
    public UserResponse create(
        UserCreateRequest request
    ) {

        String email =
            normalizeEmail(
                request.email()
            );

        if (
            userRepository
                .existsByEmailIgnoreCase(
                    email
                )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already exists"
            );
        }

        String passwordHash =
            passwordEncoder.encode(
                request.password()
            );

        AppUser user =
            new AppUser(
                email,
                passwordHash,
                request.role()
            );

        AppUser saved =
            userRepository.save(
                user
            );

        return UserResponse.from(
            saved
        );
    }

    @Transactional
    public UserResponse updateActive(
        Long userId,
        UserActiveRequest request,
        String currentUserEmail
    ) {

        AppUser user =
            userRepository
                .findById(userId)
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                        )
                );

        if (
            user.getEmail()
                .equalsIgnoreCase(
                    currentUserEmail
                )
            &&
            !request.active()
        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot deactivate your own account"
            );
        }

        user.setActive(
            request.active()
        );

        return UserResponse.from(
            user
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