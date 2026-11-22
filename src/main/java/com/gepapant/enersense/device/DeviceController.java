package com.gepapant.enersense.device;

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
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(
        DeviceService service
    ) {
        this.service = service;
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(
        @Valid
        @RequestBody DeviceCreateRequest request
    ) {
        return service.create(request);
    }

    @GetMapping("/devices")
    public List<DeviceResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/facilities/{facilityId}/devices")
    public List<DeviceResponse> findByFacility(
        @PathVariable Long facilityId
    ) {
        return service.findByFacility(
            facilityId
        );
    }
}