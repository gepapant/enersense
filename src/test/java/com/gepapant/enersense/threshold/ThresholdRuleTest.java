package com.gepapant.enersense.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.gepapant.enersense.alert.AlertSeverity;
import com.gepapant.enersense.device.Device;

class ThresholdRuleTest {

    private Device device;
    private AlertSeverity severity;

    @BeforeEach
    void setUp() {

        device = mock(Device.class);

        severity =
                AlertSeverity.values()[0];
    }

    @Test
    void constructorShouldSetFieldsAndActivateRule() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.POWER,
                        ComparisonOperator.GREATER_THAN,
                        100.0,
                        severity
                );

        assertSame(
                device,
                rule.getDevice()
        );

        assertEquals(
                Metric.POWER,
                rule.getMetric()
        );

        assertEquals(
                ComparisonOperator.GREATER_THAN,
                rule.getComparisonOperator()
        );

        assertEquals(
                100.0,
                rule.getThresholdValue()
        );

        assertEquals(
                severity,
                rule.getSeverity()
        );

        assertTrue(
                rule.isActive()
        );
    }

    @Test
    void greaterThanShouldOnlyViolateAboveThreshold() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.POWER,
                        ComparisonOperator.GREATER_THAN,
                        100.0,
                        severity
                );

        assertTrue(
                rule.isViolated(101.0)
        );

        assertFalse(
                rule.isViolated(100.0)
        );

        assertFalse(
                rule.isViolated(99.0)
        );
    }

    @Test
    void greaterThanOrEqualShouldViolateAtAndAboveThreshold() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.VOLTAGE,
                        ComparisonOperator.GREATER_THAN_OR_EQUAL,
                        230.0,
                        severity
                );

        assertTrue(
                rule.isViolated(231.0)
        );

        assertTrue(
                rule.isViolated(230.0)
        );

        assertFalse(
                rule.isViolated(229.0)
        );
    }

    @Test
    void lessThanShouldOnlyViolateBelowThreshold() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.CURRENT,
                        ComparisonOperator.LESS_THAN,
                        10.0,
                        severity
                );

        assertTrue(
                rule.isViolated(9.0)
        );

        assertFalse(
                rule.isViolated(10.0)
        );

        assertFalse(
                rule.isViolated(11.0)
        );
    }

    @Test
    void lessThanOrEqualShouldViolateAtAndBelowThreshold() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.TEMPERATURE,
                        ComparisonOperator.LESS_THAN_OR_EQUAL,
                        50.0,
                        severity
                );

        assertTrue(
                rule.isViolated(49.0)
        );

        assertTrue(
                rule.isViolated(50.0)
        );

        assertFalse(
                rule.isViolated(51.0)
        );
    }

    @Test
    void setActiveShouldDeactivateRule() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.POWER,
                        ComparisonOperator.GREATER_THAN,
                        100.0,
                        severity
                );

        rule.setActive(false);

        assertFalse(
                rule.isActive()
        );
    }

    @Test
    void setActiveShouldReactivateRule() {

        ThresholdRule rule =
                new ThresholdRule(
                        device,
                        Metric.POWER,
                        ComparisonOperator.GREATER_THAN,
                        100.0,
                        severity
                );

        rule.setActive(false);
        rule.setActive(true);

        assertTrue(
                rule.isActive()
        );
    }
}