package com.gepapant.enersense.user;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {

        userService =
                new UserService(
                        userRepository,
                        passwordEncoder
                );
    }

    @Test
    void findAllShouldReturnUsersOrderedById() {

        AppUser second =
                mock(AppUser.class);

        AppUser first =
                mock(AppUser.class);

        when(second.getId())
                .thenReturn(2L);

        when(second.getEmail())
                .thenReturn(
                        "operator@enersense.local"
                );

        when(second.getRole())
                .thenReturn(
                        Role.OPERATOR
                );

        when(second.isActive())
                .thenReturn(true);

        when(first.getId())
                .thenReturn(1L);

        when(first.getEmail())
                .thenReturn(
                        "admin@enersense.local"
                );

        when(first.getRole())
                .thenReturn(
                        Role.ADMIN
                );

        when(first.isActive())
                .thenReturn(true);

        when(userRepository.findAll())
                .thenReturn(
                        List.of(
                                second,
                                first
                        )
                );

        List<UserResponse> result =
                userService.findAll();

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                1L,
                result.get(0).id()
        );

        assertEquals(
                "admin@enersense.local",
                result.get(0).email()
        );

        assertEquals(
                Role.ADMIN,
                result.get(0).role()
        );

        assertEquals(
                2L,
                result.get(1).id()
        );

        assertEquals(
                "operator@enersense.local",
                result.get(1).email()
        );

        verify(userRepository)
                .findAll();
    }

    @Test
    void createShouldNormalizeEmailEncodePasswordAndSaveUser() {

        UserCreateRequest request =
                new UserCreateRequest(
                        "  Operator@EnerSense.Local  ",
                        "password123",
                        Role.OPERATOR
                );

        when(
                userRepository
                        .existsByEmailIgnoreCase(
                                "operator@enersense.local"
                        )
        )
                .thenReturn(false);

        when(
                passwordEncoder.encode(
                        "password123"
                )
        )
                .thenReturn(
                        "encoded-password"
                );

        when(
                userRepository.save(
                        any(AppUser.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        UserResponse response =
                userService.create(request);

        assertNotNull(response);

        assertEquals(
                "operator@enersense.local",
                response.email()
        );

        assertEquals(
                Role.OPERATOR,
                response.role()
        );

        assertTrue(
                response.active()
        );

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(
                        AppUser.class
                );

        verify(userRepository)
                .save(
                        captor.capture()
                );

        AppUser savedUser =
                captor.getValue();

        assertEquals(
                "operator@enersense.local",
                savedUser.getEmail()
        );

        assertEquals(
                "encoded-password",
                savedUser.getPasswordHash()
        );

        assertEquals(
                Role.OPERATOR,
                savedUser.getRole()
        );

        assertTrue(
                savedUser.isActive()
        );

        verify(passwordEncoder)
                .encode(
                        "password123"
                );
    }

    @Test
    void createShouldFailWhenEmailAlreadyExists() {

        UserCreateRequest request =
                new UserCreateRequest(
                        "admin@enersense.local",
                        "password123",
                        Role.ADMIN
                );

        when(
                userRepository
                        .existsByEmailIgnoreCase(
                                "admin@enersense.local"
                        )
        )
                .thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                userService.create(
                                        request
                                )
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                exception
                        .getStatusCode()
                        .value()
        );

        assertEquals(
                "Email already exists",
                exception.getReason()
        );

        verify(
                passwordEncoder,
                never()
        )
                .encode(
                        any(String.class)
                );

        verify(
                userRepository,
                never()
        )
                .save(
                        any(AppUser.class)
                );
    }

    @Test
    void updateActiveShouldDeactivateAnotherUser() {

        AppUser operator =
                new AppUser(
                        "operator@enersense.local",
                        "hashed-password",
                        Role.OPERATOR
                );

        when(userRepository.findById(2L))
                .thenReturn(
                        Optional.of(operator)
                );

        UserActiveRequest request =
                new UserActiveRequest(false);

        UserResponse response =
                userService.updateActive(
                        2L,
                        request,
                        "admin@enersense.local"
                );

        assertNotNull(response);

        assertFalse(
                response.active()
        );

        assertFalse(
                operator.isActive()
        );

        verify(userRepository)
                .findById(2L);
    }

    @Test
    void updateActiveShouldActivateAnotherUser() {

        AppUser operator =
                new AppUser(
                        "operator@enersense.local",
                        "hashed-password",
                        Role.OPERATOR
                );

        operator.setActive(false);

        when(userRepository.findById(2L))
                .thenReturn(
                        Optional.of(operator)
                );

        UserActiveRequest request =
                new UserActiveRequest(true);

        UserResponse response =
                userService.updateActive(
                        2L,
                        request,
                        "admin@enersense.local"
                );

        assertTrue(
                response.active()
        );

        assertTrue(
                operator.isActive()
        );
    }

    @Test
    void updateActiveShouldBlockSelfDeactivation() {

        AppUser admin =
                new AppUser(
                        "admin@enersense.local",
                        "hashed-password",
                        Role.ADMIN
                );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(admin)
                );

        UserActiveRequest request =
                new UserActiveRequest(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                userService.updateActive(
                                        1L,
                                        request,
                                        "ADMIN@ENERSENSE.LOCAL"
                                )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                exception
                        .getStatusCode()
                        .value()
        );

        assertEquals(
                "You cannot deactivate your own account",
                exception.getReason()
        );

        assertTrue(
                admin.isActive()
        );
    }

    @Test
    void updateActiveShouldFailWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(
                        Optional.empty()
                );

        UserActiveRequest request =
                new UserActiveRequest(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                userService.updateActive(
                                        999L,
                                        request,
                                        "admin@enersense.local"
                                )
                );

        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                exception
                        .getStatusCode()
                        .value()
        );

        assertEquals(
                "User not found",
                exception.getReason()
        );
    }
}