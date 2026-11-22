package com.gepapant.enersense.mqtt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.gepapant.enersense.telemetry.TelemetryCreateRequest;
import com.gepapant.enersense.telemetry.TelemetryService;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class MqttTelemetryListenerTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private TelemetryService telemetryService;

    @Mock
    private TelemetryCreateRequest telemetryRequest;

    private MqttTelemetryListener listener;

    @BeforeEach
    void setUp() {

        listener =
                new MqttTelemetryListener(
                        jsonMapper,
                        telemetryService
                );
    }

    @Test
    void handleShouldParseValidMessageAndCreateTelemetry()
            throws Exception {

        String payload =
                """
                {
                    "power": 2100,
                    "voltage": 230,
                    "current": 9.1,
                    "temperature": 84
                }
                """;

        Message<String> message =
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(
                                MqttHeaders.RECEIVED_TOPIC,
                                "enersense/devices/1/telemetry"
                        )
                        .build();

        when(
                jsonMapper.readValue(
                        payload,
                        TelemetryCreateRequest.class
                )
        )
                .thenReturn(
                        telemetryRequest
                );

        listener.handle(message);

        verify(jsonMapper)
                .readValue(
                        payload,
                        TelemetryCreateRequest.class
                );

        verify(telemetryService)
                .create(
                        1L,
                        telemetryRequest
                );
    }

    @Test
    void handleShouldExtractCorrectDeviceId()
            throws Exception {

        String payload =
                """
                {
                    "power": 1000,
                    "voltage": 230,
                    "current": 4.3,
                    "temperature": 35
                }
                """;

        Message<String> message =
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(
                                MqttHeaders.RECEIVED_TOPIC,
                                "enersense/devices/25/telemetry"
                        )
                        .build();

        when(
                jsonMapper.readValue(
                        payload,
                        TelemetryCreateRequest.class
                )
        )
                .thenReturn(
                        telemetryRequest
                );

        listener.handle(message);

        verify(telemetryService)
                .create(
                        25L,
                        telemetryRequest
                );
    }

    @Test
    void handleShouldIgnoreInvalidTopic() {

        Message<String> message =
                MessageBuilder
                        .withPayload(
                                """
                                {
                                    "power": 1000
                                }
                                """
                        )
                        .setHeader(
                                MqttHeaders.RECEIVED_TOPIC,
                                "invalid/topic"
                        )
                        .build();

        listener.handle(message);

        verify(
                telemetryService,
                never()
        )
                .create(
                        any(Long.class),
                        any(TelemetryCreateRequest.class)
                );
    }

    @Test
    void handleShouldIgnoreInvalidDeviceId() {

        Message<String> message =
                MessageBuilder
                        .withPayload(
                                """
                                {
                                    "power": 1000
                                }
                                """
                        )
                        .setHeader(
                                MqttHeaders.RECEIVED_TOPIC,
                                "enersense/devices/abc/telemetry"
                        )
                        .build();

        listener.handle(message);

        verify(
                telemetryService,
                never()
        )
                .create(
                        any(Long.class),
                        any(TelemetryCreateRequest.class)
                );
    }

    @Test
    void handleShouldIgnoreInvalidJson()
            throws Exception {

        String payload =
                "{power:2100}";

        Message<String> message =
                MessageBuilder
                        .withPayload(payload)
                        .setHeader(
                                MqttHeaders.RECEIVED_TOPIC,
                                "enersense/devices/1/telemetry"
                        )
                        .build();

        when(
                jsonMapper.readValue(
                        payload,
                        TelemetryCreateRequest.class
                )
        )
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid JSON"
                        )
                );

        listener.handle(message);

        verify(
                telemetryService,
                never()
        )
                .create(
                        eq(1L),
                        any(TelemetryCreateRequest.class)
                );
    }

    @Test
    void handleShouldIgnoreMissingTopic() {

        Message<String> message =
                MessageBuilder
                        .withPayload(
                                """
                                {
                                    "power": 1000
                                }
                                """
                        )
                        .build();

        listener.handle(message);

        verify(
                telemetryService,
                never()
        )
                .create(
                        any(Long.class),
                        any(TelemetryCreateRequest.class)
                );
    }
}