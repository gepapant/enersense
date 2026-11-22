package com.gepapant.enersense.device;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gepapant.enersense.facility.Facility;
import com.gepapant.enersense.facility.FacilityRepository;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final FacilityRepository facilityRepository;

    public DeviceService(
        DeviceRepository deviceRepository,
        FacilityRepository facilityRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.facilityRepository = facilityRepository;
    }

    @Transactional
    public DeviceResponse create(
        DeviceCreateRequest request
    ) {
        Facility facility = facilityRepository
            .findById(request.facilityId())
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Facility not found"
                )
            );

        Device device = new Device(
            request.name().trim(),
            request.type(),
            facility
        );

        Device saved =
            deviceRepository.save(device);

        return DeviceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll() {
        return deviceRepository
            .findAllByOrderByIdAsc()
            .stream()
            .map(DeviceResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findByFacility(
        Long facilityId
    ) {
        return deviceRepository
            .findByFacilityIdOrderByIdAsc(facilityId)
            .stream()
            .map(DeviceResponse::from)
            .toList();
    }
}