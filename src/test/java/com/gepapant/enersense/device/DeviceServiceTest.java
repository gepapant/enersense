package com.gepapant.enersense.device;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gepapant.enersense.facility.Facility;
import com.gepapant.enersense.facility.FacilityRepository;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private DeviceCreateRequest request;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {

        deviceService = new DeviceService(
                deviceRepository,
                facilityRepository
        );
    }

    @Test
    void createShouldFindFacilitySaveDeviceAndReturnResponse() {

        Facility facility = new Facility(
                "Factory A",
                "Thessaloniki"
        );

        DeviceType deviceType =
                DeviceType.values()[0];

        when(request.facilityId())
                .thenReturn(1L);

        when(request.name())
                .thenReturn("Temperature Sensor");

        when(request.type())
                .thenReturn(deviceType);

        when(facilityRepository.findById(1L))
                .thenReturn(
                        Optional.of(facility)
                );

        when(deviceRepository.save(any(Device.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        DeviceResponse response =
                deviceService.create(request);

        assertNotNull(response);

        verify(facilityRepository)
                .findById(1L);

        verify(deviceRepository)
                .save(any(Device.class));
    }

    @Test
    void createShouldTrimDeviceName() {

        Facility facility = new Facility(
                "Factory A",
                "Thessaloniki"
        );

        DeviceType deviceType =
                DeviceType.values()[0];

        when(request.facilityId())
                .thenReturn(1L);

        when(request.name())
                .thenReturn(
                        "  Temperature Sensor  "
                );

        when(request.type())
                .thenReturn(deviceType);

        when(facilityRepository.findById(1L))
                .thenReturn(
                        Optional.of(facility)
                );

        when(deviceRepository.save(any(Device.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        DeviceResponse response =
                deviceService.create(request);

        assertNotNull(response);

        assertEquals(
                "Temperature Sensor",
                response.name()
        );

        verify(deviceRepository)
                .save(any(Device.class));
    }

    @Test
    void createShouldFailWhenFacilityDoesNotExist() {

        when(request.facilityId())
                .thenReturn(999L);

        when(facilityRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> deviceService.create(request)
                );

        assertEquals(
                "Facility not found",
                exception.getMessage()
        );

        verify(deviceRepository, never())
                .save(any(Device.class));
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoDevicesExist() {

        when(deviceRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of());

        List<DeviceResponse> result =
                deviceService.findAll();

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(deviceRepository)
                .findAllByOrderByIdAsc();
    }

    @Test
    void findByFacilityShouldUseFacilityId() {

        when(
                deviceRepository
                        .findByFacilityIdOrderByIdAsc(5L)
        )
                .thenReturn(List.of());

        List<DeviceResponse> result =
                deviceService.findByFacility(5L);

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(deviceRepository)
                .findByFacilityIdOrderByIdAsc(5L);
    }
}