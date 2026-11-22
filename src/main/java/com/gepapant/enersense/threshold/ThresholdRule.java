package com.gepapant.enersense.threshold;

import com.gepapant.enersense.alert.AlertSeverity;
import com.gepapant.enersense.device.Device;

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
@Table(name = "threshold_rules")
public class ThresholdRule {

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "device_id",
        nullable = false
    )
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "comparison_operator",
        nullable = false
    )
    private ComparisonOperator comparisonOperator;

    @Column(
        name = "threshold_value",
        nullable = false
    )
    private double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private boolean active;

    protected ThresholdRule() {
    }

    public ThresholdRule(
        Device device,
        Metric metric,
        ComparisonOperator comparisonOperator,
        double thresholdValue,
        AlertSeverity severity
    ) {
        this.device = device;
        this.metric = metric;
        this.comparisonOperator =
            comparisonOperator;
        this.thresholdValue =
            thresholdValue;
        this.severity = severity;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public Metric getMetric() {
        return metric;
    }

    public ComparisonOperator
        getComparisonOperator() {

        return comparisonOperator;
    }

    public double getThresholdValue() {
        return thresholdValue;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(
        boolean active
    ) {
        this.active = active;
    }

    public boolean isViolated(
        double value
    ) {

        return switch (
            comparisonOperator
        ) {

            case GREATER_THAN ->
                value > thresholdValue;

            case GREATER_THAN_OR_EQUAL ->
                value >= thresholdValue;

            case LESS_THAN ->
                value < thresholdValue;

            case LESS_THAN_OR_EQUAL ->
                value <= thresholdValue;
        };
    }
}