package com.gepapant.enersense.threshold;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

@ExtendWith(MockitoExtension.class)
class ThresholdRuleServiceTest {

    @Mock
    private ThresholdRuleRepository thresholdRuleRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ThresholdRuleCreateRequest createRequest;

    @Mock
    private ThresholdRuleActiveRequest activeRequest;

    private ThresholdRuleService thresholdRuleService;

    @BeforeEach
    void setUp() {

        thresholdRuleService =
                new ThresholdRuleService(
                        thresholdRuleRepository,
                        deviceRepository
                );
    }

    @Test
    void createShouldFindDeviceAndSaveRule() {

        Device device =
                org.mockito.Mockito.mock(
                        Device.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        when(deviceRepository.findById(1L))
                .thenReturn(
                        Optional.of(device)
                );

        when(
                thresholdRuleRepository.save(
                        any(ThresholdRule.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        ThresholdRuleResponse response =
                thresholdRuleService.create(
                        1L,
                        createRequest
                );

        assertNotNull(response);

        verify(deviceRepository)
                .findById(1L);

        verify(thresholdRuleRepository)
                .save(
                        any(ThresholdRule.class)
                );
    }

    @Test
    void createShouldFailWhenDeviceDoesNotExist() {

        when(deviceRepository.findById(999L))
                .thenReturn(
                        Optional.empty()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thresholdRuleService.create(
                                        999L,
                                        createRequest
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
                .save(
                        any(ThresholdRule.class)
                );
    }

    @Test
    void findByDeviceShouldReturnEmptyList() {

        when(deviceRepository.existsById(1L))
                .thenReturn(true);

        when(
                thresholdRuleRepository
                        .findByDeviceIdOrderByIdAsc(1L)
        )
                .thenReturn(
                        List.of()
                );

        List<ThresholdRuleResponse> result =
                thresholdRuleService
                        .findByDevice(1L);

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );

        verify(deviceRepository)
                .existsById(1L);

        verify(thresholdRuleRepository)
                .findByDeviceIdOrderByIdAsc(1L);
    }

    @Test
    void findByDeviceShouldFailWhenDeviceDoesNotExist() {

        when(deviceRepository.existsById(999L))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thresholdRuleService
                                        .findByDevice(999L)
                );

        assertEquals(
                "Device not found",
                exception.getMessage()
        );

        verify(
                thresholdRuleRepository,
                never()
        )
                .findByDeviceIdOrderByIdAsc(
                        999L
                );
    }

    @Test
    void updateActiveShouldUpdateRule() {

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        when(
                thresholdRuleRepository
                        .findById(5L)
        )
                .thenReturn(
                        Optional.of(rule)
                );

        when(activeRequest.active())
                .thenReturn(true);

        ThresholdRuleResponse response =
                thresholdRuleService
                        .updateActive(
                                5L,
                                activeRequest
                        );

        assertNotNull(response);

        verify(rule)
                .setActive(true);
    }

    @Test
    void updateActiveShouldFailWhenRuleDoesNotExist() {

        when(
                thresholdRuleRepository
                        .findById(999L)
        )
                .thenReturn(
                        Optional.empty()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thresholdRuleService
                                        .updateActive(
                                                999L,
                                                activeRequest
                                        )
                );

        assertEquals(
                "Threshold rule not found",
                exception.getMessage()
        );
    }

    @Test
    void deleteShouldDeleteRule() {

        ThresholdRule rule =
                org.mockito.Mockito.mock(
                        ThresholdRule.class
                );

        when(
                thresholdRuleRepository
                        .findById(5L)
        )
                .thenReturn(
                        Optional.of(rule)
                );

        thresholdRuleService.delete(5L);

        verify(thresholdRuleRepository)
                .findById(5L);

        verify(thresholdRuleRepository)
                .delete(rule);
    }

    @Test
    void deleteShouldFailWhenRuleDoesNotExist() {

        when(
                thresholdRuleRepository
                        .findById(999L)
        )
                .thenReturn(
                        Optional.empty()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                thresholdRuleService
                                        .delete(999L)
                );

        assertEquals(
                "Threshold rule not found",
                exception.getMessage()
        );

        verify(
                thresholdRuleRepository,
                never()
        )
                .delete(
                        any(ThresholdRule.class)
                );
    }
}