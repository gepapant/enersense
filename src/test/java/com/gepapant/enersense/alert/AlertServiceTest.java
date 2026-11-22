package com.gepapant.enersense.alert;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {

        alertService =
                new AlertService(repository);
    }

    @Test
    void findAllShouldReturnAlertsInRepositoryOrder() {

        Alert first =
                org.mockito.Mockito.mock(
                        Alert.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        Alert second =
                org.mockito.Mockito.mock(
                        Alert.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        when(
                repository
                        .findAllByOrderByCreatedAtDesc()
        )
                .thenReturn(
                        List.of(
                                first,
                                second
                        )
                );

        List<AlertResponse> result =
                alertService.findAll();

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        verify(repository)
                .findAllByOrderByCreatedAtDesc();
    }

    @Test
    void acknowledgeShouldAcknowledgeAlert() {

        Alert alert =
                org.mockito.Mockito.mock(
                        Alert.class,
                        Answers.RETURNS_DEEP_STUBS
                );

        when(repository.findById(5L))
                .thenReturn(
                        Optional.of(alert)
                );

        AlertResponse response =
                alertService.acknowledge(5L);

        assertNotNull(response);

        verify(repository)
                .findById(5L);

        verify(alert)
                .acknowledge();
    }

    @Test
    void acknowledgeShouldFailWhenAlertDoesNotExist() {

        when(repository.findById(999L))
                .thenReturn(
                        Optional.empty()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                alertService
                                        .acknowledge(999L)
                );

        assertEquals(
                "Alert not found",
                exception.getMessage()
        );
    }
}