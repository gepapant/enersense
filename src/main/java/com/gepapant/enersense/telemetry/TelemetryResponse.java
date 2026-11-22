package com.gepapant.enersense.telemetry;

import java.time.Instant;

public record TelemetryResponse(
    String id,
    Long deviceId,
    double power,
    double voltage,
    double current,
    double temperature,
    Instant recordedAt
) {

    public static TelemetryResponse from(
        TelemetryReading reading
    ) {
        return new TelemetryResponse(
            reading.getId(),
            reading.getDeviceId(),
            reading.getPower(),
            reading.getVoltage(),
            reading.getCurrent(),
            reading.getTemperature(),
            reading.getRecordedAt()
        );
    }
}