package com.gepapant.enersense.telemetry;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gepapant.enersense.alert.AlertEvaluationService;
import com.gepapant.enersense.device.DeviceRepository;
import com.gepapant.enersense.realtime.LiveEventService;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final DeviceRepository deviceRepository;
    private final AlertEvaluationService alertEvaluationService;
    private final LiveEventService liveEventService;

    public TelemetryService(
        TelemetryRepository telemetryRepository,
        DeviceRepository deviceRepository,
        AlertEvaluationService alertEvaluationService,
        LiveEventService liveEventService
    ) {
        this.telemetryRepository =
            telemetryRepository;

        this.deviceRepository =
            deviceRepository;

        this.alertEvaluationService =
            alertEvaluationService;

        this.liveEventService =
            liveEventService;
    }

    public TelemetryResponse create(
        Long deviceId,
        TelemetryCreateRequest request
    ) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new IllegalArgumentException(
                "Device not found"
            );
        }

        TelemetryReading reading =
            new TelemetryReading(
                deviceId,
                request.power(),
                request.voltage(),
                request.current(),
                request.temperature(),
                Instant.now()
            );

        TelemetryReading saved =
            telemetryRepository.save(reading);

        TelemetryResponse response =
            TelemetryResponse.from(saved);

        liveEventService.sendTelemetry(
            response
        );

        alertEvaluationService.evaluate(
            deviceId,
            saved
        );

        return response;
    }

    public List<TelemetryResponse> findByDevice(
        Long deviceId
    ) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new IllegalArgumentException(
                "Device not found"
            );
        }

        return telemetryRepository
            .findByDeviceIdOrderByRecordedAtDesc(
                deviceId
            )
            .stream()
            .map(TelemetryResponse::from)
            .toList();
    }
}