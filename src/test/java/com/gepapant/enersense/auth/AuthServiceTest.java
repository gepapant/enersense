package com.gepapant.enersense.auth;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import com.gepapant.enersense.user.AppUser;
import com.gepapant.enersense.user.AppUserRepository;
import com.gepapant.enersense.user.Role;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void bootstrapShouldCreateFirstAdminUser() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Jwt jwt = mockJwt();

        when(jwtTokenService.issueToken(any(AppUser.class)))
                .thenReturn(jwt);

        BootstrapRequest request = new BootstrapRequest(
                " Admin@EnerSense.Local ",
                "password123"
        );

        AuthResponse response = authService.bootstrap(request);

        assertNotNull(response);
        assertEquals("test-token", response.token());
        assertEquals(
                "admin@enersense.local",
                response.user().email()
        );
        assertEquals(Role.ADMIN, response.user().role());
        assertTrue(response.user().active());

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertEquals(
                "admin@enersense.local",
                savedUser.getEmail()
        );

        assertEquals(
                "hashed-password",
                savedUser.getPasswordHash()
        );

        assertEquals(Role.ADMIN, savedUser.getRole());

        verify(passwordEncoder)
                .encode("password123");
    }

    @Test
    void bootstrapShouldFailWhenUsersAlreadyExist() {
        when(userRepository.count()).thenReturn(1L);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.bootstrap(
                                new BootstrapRequest(
                                        "admin@enersense.local",
                                        "password123"
                                )
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(userRepository, never())
                .save(any(AppUser.class));

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void loginShouldNormalizeEmailAndReturnToken() {
        AppUser user = new AppUser(
                "admin@enersense.local",
                "hashed-password",
                Role.ADMIN
        );

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@enersense.local"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "password123",
                        "hashed-password"
                )
        ).thenReturn(true);

        Jwt jwt = mockJwt();

        when(jwtTokenService.issueToken(user))
                .thenReturn(jwt);

        LoginRequest request = new LoginRequest(
                " Admin@EnerSense.Local ",
                "password123"
        );

        AuthResponse response =
                authService.login(request);

        assertEquals(
                "test-token",
                response.token()
        );

        assertEquals(
                "admin@enersense.local",
                response.user().email()
        );

        assertEquals(
                Role.ADMIN,
                response.user().role()
        );

        verify(userRepository)
                .findByEmailIgnoreCase(
                        "admin@enersense.local"
                );
    }

    @Test
    void loginShouldFailWithWrongPassword() {
        AppUser user = new AppUser(
                "admin@enersense.local",
                "hashed-password",
                Role.ADMIN
        );

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@enersense.local"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "wrong-password",
                        "hashed-password"
                )
        ).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "admin@enersense.local",
                                        "wrong-password"
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void loginShouldFailWhenUserDoesNotExist() {
        when(
                userRepository.findByEmailIgnoreCase(
                        "missing@enersense.local"
                )
        ).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(
                                new LoginRequest(
                                        "missing@enersense.local",
                                        "password123"
                                )
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void meShouldReturnCurrentUser() {
        AppUser user = new AppUser(
                "admin@enersense.local",
                "hashed-password",
                Role.ADMIN
        );

        when(
                userRepository.findByEmailIgnoreCase(
                        "admin@enersense.local"
                )
        ).thenReturn(Optional.of(user));

        var response =
                authService.me(
                        "admin@enersense.local"
                );

        assertEquals(
                "admin@enersense.local",
                response.email()
        );

        assertEquals(
                Role.ADMIN,
                response.role()
        );

        assertTrue(response.active());
    }

    private Jwt mockJwt() {
        Jwt jwt = mock(Jwt.class);

        Instant expiresAt =
                Instant.now().plusSeconds(7200);

        when(jwt.getTokenValue())
                .thenReturn("test-token");

        when(jwt.getExpiresAt())
                .thenReturn(expiresAt);

        return jwt;
    }
}