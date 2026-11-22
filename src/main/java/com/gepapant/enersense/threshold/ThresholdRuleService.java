package com.gepapant.enersense.threshold;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gepapant.enersense.device.Device;
import com.gepapant.enersense.device.DeviceRepository;

@Service
public class ThresholdRuleService {

    private final ThresholdRuleRepository
        thresholdRuleRepository;

    private final DeviceRepository
        deviceRepository;

    public ThresholdRuleService(
        ThresholdRuleRepository
            thresholdRuleRepository,
        DeviceRepository
            deviceRepository
    ) {
        this.thresholdRuleRepository =
            thresholdRuleRepository;

        this.deviceRepository =
            deviceRepository;
    }

    @Transactional
    public ThresholdRuleResponse create(
        Long deviceId,
        ThresholdRuleCreateRequest request
    ) {

        Device device =
            deviceRepository
                .findById(deviceId)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Device not found"
                        )
                );

        ThresholdRule rule =
            new ThresholdRule(
                device,
                request.metric(),
                request.comparisonOperator(),
                request.thresholdValue(),
                request.severity()
            );

        ThresholdRule saved =
            thresholdRuleRepository.save(
                rule
            );

        return ThresholdRuleResponse
            .from(saved);
    }

    @Transactional(readOnly = true)
    public List<ThresholdRuleResponse>
        findByDevice(
            Long deviceId
        ) {

        if (
            !deviceRepository
                .existsById(deviceId)
        ) {

            throw new IllegalArgumentException(
                "Device not found"
            );
        }

        return thresholdRuleRepository
            .findByDeviceIdOrderByIdAsc(
                deviceId
            )
            .stream()
            .map(
                ThresholdRuleResponse::from
            )
            .toList();
    }

    @Transactional
    public ThresholdRuleResponse
        updateActive(
            Long ruleId,
            ThresholdRuleActiveRequest request
        ) {

        ThresholdRule rule =
            thresholdRuleRepository
                .findById(ruleId)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Threshold rule not found"
                        )
                );

        rule.setActive(
            request.active()
        );

        return ThresholdRuleResponse
            .from(rule);
    }

    @Transactional
    public void delete(
        Long ruleId
    ) {

        ThresholdRule rule =
            thresholdRuleRepository
                .findById(ruleId)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Threshold rule not found"
                        )
                );

        thresholdRuleRepository.delete(
            rule
        );
    }
}