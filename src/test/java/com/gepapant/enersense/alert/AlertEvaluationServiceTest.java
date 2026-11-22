package com.gepapant.enersense.alert;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gepapant.enersense.device.Device;
import com.gepapant.enersense.device.DeviceRepository;
import com.gepapant.enersense.realtime.LiveEventService;
import com.gepapant.enersense.telemetry.TelemetryReading;
import com.gepapant.enersense.threshold.Metric;
import com.gepapant.enersense.threshold.ThresholdRule;
import com.gepapant.enersense.threshold.ThresholdRuleRepository;

@ExtendWith(MockitoExtension.class)
class AlertEvaluationServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ThresholdRuleRepository thresholdRuleRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private LiveEventService liveEventService;

    @Mock
    private TelemetryReading reading;

    private AlertEvaluationService alertEvaluationService;

    @BeforeEach
    void setUp() {

        alertEvaluationService =
                new AlertEvaluationService(
                        deviceRepository,
                        thresholdRuleRepository,
                        alertRepository,
                        liveEventService
                );
    }

    @Test
    void evaluateShouldFailWhenDeviceDoesNotExist() {

        when(deviceRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                alertEvaluationService.evaluate(
                                        999L,
                                        reading
                                )
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(
                thresholdRuleRepository,
                never()
        )
                .findByDeviceIdAndActiveTrueOrderByIdAsc(
                        999L
                );

        verify(
                alertRepository,
                never()
        )
                .save(any(Alert.class));
    }

    @Test
    void evaluateShouldDoNothingWhenNoActiveRulesExist() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository
                        .findByDeviceIdAndActiveTrueOrderByIdAsc(
                                1L
                        )
        )
                .thenReturn(
                        List.of()
                );

        alertEvaluationService.evaluate(
                1L,
                reading
        );

        verify(
                alertRepository,
                never()
        )
                .save(any(Alert.class));

        verify(
                liveEventService,
                never()
        )
                .sendAlert(
                        any(AlertResponse.class)
                );
    }

    @Test
    void evaluateShouldCreateAlertWhenPowerRuleIsViolated() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository
                        .findByDeviceIdAndActiveTrueOrderByIdAsc(
                                1L
                        )
        )
                .thenReturn(
                        List.of(rule)
                );

        when(rule.getMetric())
                .thenReturn(
                        Metric.POWER
                );

        when(rule.getSeverity())
                .thenReturn(
                        AlertSeverity.WARNING
                );

        when(reading.getPower())
                .thenReturn(
                        150.0
                );

        when(rule.isViolated(150.0))
                .thenReturn(true);

        when(
                alertRepository.save(
                        any(Alert.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        alertEvaluationService.evaluate(
                1L,
                reading
        );

        verify(rule)
                .isViolated(150.0);

        verify(alertRepository)
                .save(
                        any(Alert.class)
                );

        verify(liveEventService)
                .sendAlert(
                        any(AlertResponse.class)
                );
    }

    @Test
    void evaluateShouldNotCreateAlertWhenVoltageRuleIsNotViolated() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class
                );

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository
                        .findByDeviceIdAndActiveTrueOrderByIdAsc(
                                1L
                        )
        )
                .thenReturn(
                        List.of(rule)
                );

        when(rule.getMetric())
                .thenReturn(
                        Metric.VOLTAGE
                );

        when(reading.getVoltage())
                .thenReturn(
                        230.0
                );

        when(rule.isViolated(230.0))
                .thenReturn(false);

        alertEvaluationService.evaluate(
                1L,
                reading
        );

        verify(rule)
                .isViolated(230.0);

        verify(
                alertRepository,
                never()
        )
                .save(any(Alert.class));

        verify(
                liveEventService,
                never()
        )
                .sendAlert(
                        any(AlertResponse.class)
                );
    }

    @Test
    void evaluateShouldUseCurrentReadingForCurrentMetric() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class
                );

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository
                        .findByDeviceIdAndActiveTrueOrderByIdAsc(
                                1L
                        )
        )
                .thenReturn(
                        List.of(rule)
                );

        when(rule.getMetric())
                .thenReturn(
                        Metric.CURRENT
                );

        when(reading.getCurrent())
                .thenReturn(
                        12.5
                );

        when(rule.isViolated(12.5))
                .thenReturn(false);

        alertEvaluationService.evaluate(
                1L,
                reading
        );

        verify(rule)
                .isViolated(12.5);

        verify(
                alertRepository,
                never()
        )
                .save(any(Alert.class));
    }

    @Test
    void evaluateShouldUseTemperatureReadingForTemperatureMetric() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class
                );

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository
                        .findByDeviceIdAndActiveTrueOrderByIdAsc(
                                1L
                        )
        )
                .thenReturn(
                        List.of(rule)
                );

        when(rule.getMetric())
                .thenReturn(
                        Metric.TEMPERATURE
                );

        when(reading.getTemperature())
                .thenReturn(
                        75.0
                );

        when(rule.isViolated(75.0))
                .thenReturn(false);

        alertEvaluationService.evaluate(
                1L,
                reading
        );

        verify(rule)
                .isViolated(75.0);

        verify(
                alertRepository,
                never()
        )
                .save(any(Alert.class));
    }
}