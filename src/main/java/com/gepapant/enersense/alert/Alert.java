package com.gepapant.enersense.alert;

import java.time.Instant;

import com.gepapant.enersense.device.Device;
import com.gepapant.enersense.threshold.ThresholdRule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private ThresholdRule rule;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean acknowledged;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    protected Alert() {
    }

    public Alert(
        Device device,
        ThresholdRule rule,
        String message,
        AlertSeverity severity
    ) {
        this.device = device;
        this.rule = rule;
        this.message = message;
        this.severity = severity;
        this.createdAt = Instant.now();
        this.acknowledged = false;
    }

    public Long getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public ThresholdRule getRule() {
        return rule;
    }

    public String getMessage() {
        return message;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void acknowledge() {
        this.acknowledged = true;
        this.acknowledgedAt = Instant.now();
    }
}