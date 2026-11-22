package com.gepapant.enersense.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.gepapant.enersense.user.AppUser;

@Service
public class JwtTokenService {

    private static final String ISSUER =
        "enersense-api";

    private final JwtEncoder
        jwtEncoder;

    private final long
        expirationMinutes;

    public JwtTokenService(
        JwtEncoder jwtEncoder,

        @Value(
            "${enersense.security.jwt-expiration-minutes:120}"
        )
        long expirationMinutes
    ) {
        this.jwtEncoder =
            jwtEncoder;

        this.expirationMinutes =
            expirationMinutes;
    }

    public Jwt issueToken(
        AppUser user
    ) {

        Instant now =
            Instant.now();

        Instant expiresAt =
            now.plus(
                expirationMinutes,
                ChronoUnit.MINUTES
            );

        JwtClaimsSet claims =
            JwtClaimsSet
                .builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(
                    user.getEmail()
                )
                .claim(
                    "userId",
                    user.getId()
                )
                .claim(
                    "roles",
                    List.of(
                        user
                            .getRole()
                            .name()
                    )
                )
                .build();

        return jwtEncoder.encode(
            JwtEncoderParameters
                .from(claims)
        );
    }
}