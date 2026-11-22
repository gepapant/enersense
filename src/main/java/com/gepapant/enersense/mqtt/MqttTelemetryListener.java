package com.gepapant.enersense.mqtt;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.gepapant.enersense.telemetry.TelemetryCreateRequest;
import com.gepapant.enersense.telemetry.TelemetryService;

import tools.jackson.databind.json.JsonMapper;

@Service
public class MqttTelemetryListener {

    private final JsonMapper jsonMapper;
    private final TelemetryService telemetryService;

    public MqttTelemetryListener(
        JsonMapper jsonMapper,
        TelemetryService telemetryService
    ) {
        this.jsonMapper = jsonMapper;
        this.telemetryService = telemetryService;
    }

    @ServiceActivator(
        inputChannel = "mqttInputChannel"
    )
    public void handle(
        Message<?> message
    ) {
        String topic =
            message.getHeaders().get(
                MqttHeaders.RECEIVED_TOPIC,
                String.class
            );

        if (topic == null) {
            System.err.println(
                "MQTT message ignored: topic is missing"
            );
            return;
        }

        try {
            Long deviceId =
                extractDeviceId(topic);

            String payload =
                message.getPayload().toString();

            TelemetryCreateRequest request =
                jsonMapper.readValue(
                    payload,
                    TelemetryCreateRequest.class
                );

            telemetryService.create(
                deviceId,
                request
            );

            System.out.println(
                "MQTT telemetry received for device "
                + deviceId
                + ": "
                + payload
            );

        } catch (Exception exception) {
            System.err.println(
                "MQTT message ignored: "
                + exception.getMessage()
            );
        }
    }

    private Long extractDeviceId(
        String topic
    ) {
        String[] parts =
            topic.split("/");

        if (
            parts.length != 4
            || !parts[0].equals("enersense")
            || !parts[1].equals("devices")
            || !parts[3].equals("telemetry")
        ) {
            throw new IllegalArgumentException(
                "Invalid MQTT topic: " + topic
            );
        }

        return Long.valueOf(parts[2]);
    }
}