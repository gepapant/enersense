package com.gepapant.enersense.threshold;

import com.gepapant.enersense.alert.AlertSeverity;

public record ThresholdRuleResponse(
    Long id,
    Long deviceId,
    Metric metric,
    ComparisonOperator comparisonOperator,
    double thresholdValue,
    AlertSeverity severity,
    boolean active
) {

    public static ThresholdRuleResponse from(
        ThresholdRule rule
    ) {
        return new ThresholdRuleResponse(
            rule.getId(),
            rule.getDevice().getId(),
            rule.getMetric(),
            rule.getComparisonOperator(),
            rule.getThresholdValue(),
            rule.getSeverity(),
            rule.isActive()
        );
    }
}