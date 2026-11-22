package com.gepapant.enersense.telemetry;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
    "/api/devices/{deviceId}/telemetry"
)
public class TelemetryController {

    private final TelemetryService service;

    public TelemetryController(
        TelemetryService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TelemetryResponse create(
        @PathVariable Long deviceId,
        @Valid
        @RequestBody TelemetryCreateRequest request
    ) {
        return service.create(
            deviceId,
            request
        );
    }

    @GetMapping
    public List<TelemetryResponse> findByDevice(
        @PathVariable Long deviceId
    ) {
        return service.findByDevice(
            deviceId
        );
    }
}