package com.gepapant.enersense.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService
        userService;

    public UserController(
        UserService userService
    ) {
        this.userService =
            userService;
    }

    @GetMapping
    public List<UserResponse>
        findAll() {

        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<
        UserResponse
    > create(
        @Valid
        @RequestBody
        UserCreateRequest request
    ) {

        UserResponse response =
            userService.create(
                request
            );

        return ResponseEntity
            .status(
                HttpStatus.CREATED
            )
            .body(response);
    }

    @PatchMapping(
        "/{userId}/active"
    )
    public UserResponse updateActive(
        @PathVariable
        Long userId,

        @Valid
        @RequestBody
        UserActiveRequest request,

        Authentication authentication
    ) {

        return userService
            .updateActive(
                userId,
                request,
                authentication.getName()
            );
    }
}