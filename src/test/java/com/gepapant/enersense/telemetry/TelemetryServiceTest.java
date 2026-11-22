package com.gepapant.enersense.telemetry;

import java.util.List;

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

import com.gepapant.enersense.alert.AlertEvaluationService;
import com.gepapant.enersense.device.DeviceRepository;
import com.gepapant.enersense.realtime.LiveEventService;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private TelemetryRepository telemetryRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AlertEvaluationService alertEvaluationService;

    @Mock
    private LiveEventService liveEventService;

    @Mock
    private TelemetryCreateRequest request;

    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {

        telemetryService =
                new TelemetryService(
                        telemetryRepository,
                        deviceRepository,
                        alertEvaluationService,
                        liveEventService
                );
    }

    @Test
    void createShouldSaveTelemetrySendLiveEventAndEvaluateAlerts() {

        when(deviceRepository.existsById(1L))
                .thenReturn(true);

        when(request.power())
                .thenReturn(120.0);

        when(request.voltage())
                .thenReturn(230.0);

        when(request.current())
                .thenReturn(5.2);

        when(request.temperature())
                .thenReturn(42.0);

        when(
                telemetryRepository.save(
                        any(TelemetryReading.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        TelemetryResponse response =
                telemetryService.create(
                        1L,
                        request
                );

        assertNotNull(response);

        assertEquals(
                1L,
                response.deviceId()
        );

        assertEquals(
                120.0,
                response.power()
        );

        assertEquals(
                230.0,
                response.voltage()
        );

        assertEquals(
                5.2,
                response.current()
        );

        assertEquals(
                42.0,
                response.temperature()
        );

        verify(deviceRepository)
                .existsById(1L);

        verify(telemetryRepository)
                .save(
                        any(TelemetryReading.class)
                );

        verify(liveEventService)
                .sendTelemetry(
                        any(TelemetryResponse.class)
                );

        verify(alertEvaluationService)
                .evaluate(
                        org.mockito.ArgumentMatchers.eq(1L),
                        any(TelemetryReading.class)
                );
    }

    @Test
    void createShouldFailWhenDeviceDoesNotExist() {

        when(deviceRepository.existsById(999L))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                telemetryService.create(
                                        999L,
                                        request
                                )
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(
                telemetryRepository,
                never()
        )
                .save(
                        any(TelemetryReading.class)
                );

        verify(
                liveEventService,
                never()
        )
                .sendTelemetry(
                        any(TelemetryResponse.class)
                );

        verify(
                alertEvaluationService,
                never()
        )
                .evaluate(
                        org.mockito.ArgumentMatchers.anyLong(),
                        any(TelemetryReading.class)
                );
    }

    @Test
    void findByDeviceShouldReturnTelemetry() {

        when(deviceRepository.existsById(1L))
                .thenReturn(true);

        TelemetryReading reading =
                new TelemetryReading(
                        1L,
                        100.0,
                        230.0,
                        4.5,
                        35.0,
                        java.time.Instant.now()
                );

        when(
                telemetryRepository
                        .findByDeviceIdOrderByRecordedAtDesc(
                                1L
                        )
        )
                .thenReturn(
                        List.of(reading)
                );

        List<TelemetryResponse> result =
                telemetryService
                        .findByDevice(1L);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                1L,
                result.get(0).deviceId()
        );

        verify(deviceRepository)
                .existsById(1L);

        verify(telemetryRepository)
                .findByDeviceIdOrderByRecordedAtDesc(
                        1L
                );
    }

    @Test
    void findByDeviceShouldReturnEmptyListWhenNoTelemetryExists() {

        when(deviceRepository.existsById(1L))
                .thenReturn(true);

        when(
                telemetryRepository
                        .findByDeviceIdOrderByRecordedAtDesc(
                                1L
                        )
        )
                .thenReturn(
                        List.of()
                );

        List<TelemetryResponse> result =
                telemetryService
                        .findByDevice(1L);

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void findByDeviceShouldFailWhenDeviceDoesNotExist() {

        when(deviceRepository.existsById(999L))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                telemetryService
                                        .findByDevice(999L)
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(
                telemetryRepository,
                never()
        )
                .findByDeviceIdOrderByRecordedAtDesc(
                        999L
                );
    }
}