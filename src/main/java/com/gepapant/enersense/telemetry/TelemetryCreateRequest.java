package com.gepapant.enersense.telemetry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record TelemetryCreateRequest(

    @NotNull
    @PositiveOrZero
    Double power,

    @NotNull
    @Positive
    Double voltage,

    @NotNull
    @PositiveOrZero
    Double current,

    @NotNull
    Double temperature

) {
}