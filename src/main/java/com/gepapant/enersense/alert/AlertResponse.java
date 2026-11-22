package com.gepapant.enersense.alert;

import java.time.Instant;

public record AlertResponse(
    Long id,
    Long deviceId,
    String deviceName,
    Long ruleId,
    String message,
    AlertSeverity severity,
    Instant createdAt,
    boolean acknowledged,
    Instant acknowledgedAt
) {

    public static AlertResponse from(
        Alert alert
    ) {
        return new AlertResponse(
            alert.getId(),
            alert.getDevice().getId(),
            alert.getDevice().getName(),
            alert.getRule().getId(),
            alert.getMessage(),
            alert.getSeverity(),
            alert.getCreatedAt(),
            alert.isAcknowledged(),
            alert.getAcknowledgedAt()
        );
    }
}