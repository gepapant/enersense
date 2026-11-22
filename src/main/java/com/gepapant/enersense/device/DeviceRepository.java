package com.gepapant.enersense.device;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository
    extends JpaRepository<Device, Long> {

    List<Device> findAllByOrderByIdAsc();

    List<Device> findByFacilityIdOrderByIdAsc(Long facilityId);
}