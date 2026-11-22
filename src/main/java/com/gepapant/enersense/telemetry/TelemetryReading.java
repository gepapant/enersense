package com.gepapant.enersense.telemetry;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "telemetry_readings")
public class TelemetryReading {

    @Id
    private String id;

    private Long deviceId;

    private double power;
    private double voltage;
    private double current;
    private double temperature;

    private Instant recordedAt;

    protected TelemetryReading() {
    }

    public TelemetryReading(
        Long deviceId,
        double power,
        double voltage,
        double current,
        double temperature,
        Instant recordedAt
    ) {
        this.deviceId = deviceId;
        this.power = power;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.recordedAt = recordedAt;
    }

    public String getId() {
        return id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public double getPower() {
        return power;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public double getTemperature() {
        return temperature;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}