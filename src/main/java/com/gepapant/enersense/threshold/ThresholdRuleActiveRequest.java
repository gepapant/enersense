package com.gepapant.enersense.threshold;

import jakarta.validation.constraints.NotNull;

public record ThresholdRuleActiveRequest(

    @NotNull
    Boolean active

) {
}