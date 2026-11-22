package com.gepapant.enersense.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gepapant.enersense.user.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService
        authService;

    public AuthController(
        AuthService authService
    ) {
        this.authService =
            authService;
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<AuthResponse>
        bootstrap(
            @Valid
            @RequestBody
            BootstrapRequest request
        ) {

        AuthResponse response =
            authService.bootstrap(
                request
            );

        return ResponseEntity
            .status(
                HttpStatus.CREATED
            )
            .body(response);
    }

    @PostMapping("/login")
    public AuthResponse login(
        @Valid
        @RequestBody
        LoginRequest request
    ) {

        return authService.login(
            request
        );
    }

    @GetMapping("/me")
    public UserResponse me(
        Authentication authentication
    ) {

        return authService.me(
            authentication.getName()
        );
    }
}