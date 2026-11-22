package com.gepapant.enersense.alert;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gepapant.enersense.device.Device;
import com.gepapant.enersense.device.DeviceRepository;
import com.gepapant.enersense.realtime.LiveEventService;
import com.gepapant.enersense.telemetry.TelemetryReading;
import com.gepapant.enersense.threshold.ThresholdRule;
import com.gepapant.enersense.threshold.ThresholdRuleRepository;

@Service
public class AlertEvaluationService {

    private final DeviceRepository deviceRepository;
    private final ThresholdRuleRepository ruleRepository;
    private final AlertRepository alertRepository;
    private final LiveEventService liveEventService;

    public AlertEvaluationService(
        DeviceRepository deviceRepository,
        ThresholdRuleRepository ruleRepository,
        AlertRepository alertRepository,
        LiveEventService liveEventService
    ) {
        this.deviceRepository =
            deviceRepository;

        this.ruleRepository =
            ruleRepository;

        this.alertRepository =
            alertRepository;

        this.liveEventService =
            liveEventService;
    }

    @Transactional
    public void evaluate(
        Long deviceId,
        TelemetryReading reading
    ) {
        Device device = deviceRepository
            .findById(deviceId)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Device not found"
                )
            );

        List<ThresholdRule> rules =
            ruleRepository
                .findByDeviceIdAndActiveTrueOrderByIdAsc(
                    deviceId
                );

        for (ThresholdRule rule : rules) {

            double value =
                switch (rule.getMetric()) {

                    case POWER ->
                        reading.getPower();

                    case VOLTAGE ->
                        reading.getVoltage();

                    case CURRENT ->
                        reading.getCurrent();

                    case TEMPERATURE ->
                        reading.getTemperature();
                };

            if (rule.isViolated(value)) {

                String message =
                    rule.getMetric()
                    + " value "
                    + value
                    + " violated threshold "
                    + rule.getThresholdValue();

                Alert alert =
                    new Alert(
                        device,
                        rule,
                        message,
                        rule.getSeverity()
                    );

                Alert saved =
                    alertRepository.save(alert);

                liveEventService.sendAlert(
                    AlertResponse.from(saved)
                );
            }
        }
    }
}