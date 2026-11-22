package com.gepapant.enersense.threshold;

import com.gepapant.enersense.alert.AlertSeverity;

import jakarta.validation.constraints.NotNull;

public record ThresholdRuleCreateRequest(

    @NotNull
    Metric metric,

    @NotNull
    ComparisonOperator comparisonOperator,

    @NotNull
    Double thresholdValue,

    @NotNull
    AlertSeverity severity

) {
}