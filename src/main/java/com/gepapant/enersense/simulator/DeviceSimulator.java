package com.gepapant.enersense.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.gepapant.enersense.device.DeviceResponse;
import com.gepapant.enersense.device.DeviceStatus;
import com.gepapant.enersense.device.DeviceType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class DeviceSimulator {

    private static final String BROKER =
        "tcp://localhost:1883";

    private static final String API_BASE_URL =
        "http://localhost:8080";

    private static final String LOGIN_API =
        API_BASE_URL
        + "/api/auth/login";

    private static final String DEVICES_API =
        API_BASE_URL
        + "/api/devices";

    private static final long
        PUBLISH_INTERVAL_MS =
            5000;

    private static final HttpClient
        HTTP_CLIENT =
            HttpClient.newHttpClient();

    private static final JsonMapper
        JSON_MAPPER =
            JsonMapper
                .builder()
                .build();

    private static String
        accessToken;

    public static void main(
        String[] args
    )
        throws Exception {

        String simulatorEmail =
            requireEnvironmentVariable(
                "ENERSENSE_SIMULATOR_EMAIL"
            );

        String simulatorPassword =
            requireEnvironmentVariable(
                "ENERSENSE_SIMULATOR_PASSWORD"
            );

        accessToken =
            authenticate(
                simulatorEmail,
                simulatorPassword
            );

        String clientId =
            "enersense-simulator-"
            + UUID.randomUUID();

        MqttClient mqttClient =
            new MqttClient(
                BROKER,
                clientId
            );

        MqttConnectOptions options =
            new MqttConnectOptions();

        options.setAutomaticReconnect(
            true
        );

        options.setCleanSession(
            true
        );

        mqttClient.connect(
            options
        );

        System.out.println(
            "EnerSense device simulator connected"
        );

        System.out.println(
            "Broker: "
            + BROKER
        );

        System.out.println(
            "Device source: "
            + DEVICES_API
        );

        System.out.println(
            "Authenticated as: "
            + simulatorEmail
        );

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(
                    () ->
                        closeClient(
                            mqttClient
                        )
                )
            );

        while (true) {

            try {

                DeviceResponse[] devices =
                    loadDevices(
                        simulatorEmail,
                        simulatorPassword
                    );

                int activeDevices = 0;

                for (
                    DeviceResponse device
                    : devices
                ) {

                    if (
                        device.status()
                        != DeviceStatus.ACTIVE
                    ) {

                        continue;
                    }

                    activeDevices++;

                    publishTelemetry(
                        mqttClient,
                        device
                    );
                }

                if (
                    activeDevices == 0
                ) {

                    System.out.println(
                        "No ACTIVE devices found."
                    );
                }

            } catch (
                Exception exception
            ) {

                System.err.println(
                    "Simulator cycle failed: "
                    + exception.getMessage()
                );
            }

            Thread.sleep(
                PUBLISH_INTERVAL_MS
            );
        }
    }

    private static DeviceResponse[]
        loadDevices(
            String email,
            String password
        )
        throws Exception {

        HttpResponse<String> response =
            sendDevicesRequest();

        if (
            response.statusCode()
            == 401
        ) {

            System.out.println(
                "Simulator JWT expired. Re-authenticating..."
            );

            accessToken =
                authenticate(
                    email,
                    password
                );

            response =
                sendDevicesRequest();
        }

        if (
            response.statusCode()
            != 200
        ) {

            throw new IllegalStateException(
                "Devices API returned HTTP "
                + response.statusCode()
            );
        }

        return JSON_MAPPER
            .readValue(
                response.body(),
                DeviceResponse[].class
            );
    }

    private static HttpResponse<String>
        sendDevicesRequest()
        throws Exception {

        HttpRequest request =
            HttpRequest
                .newBuilder()
                .uri(
                    URI.create(
                        DEVICES_API
                    )
                )
                .header(
                    "Accept",
                    "application/json"
                )
                .header(
                    "Authorization",
                    "Bearer "
                    + accessToken
                )
                .GET()
                .build();

        return HTTP_CLIENT.send(
            request,
            HttpResponse
                .BodyHandlers
                .ofString()
        );
    }

    private static String
        authenticate(
            String email,
            String password
        )
        throws Exception {

        String body =
            JSON_MAPPER
                .writeValueAsString(
                    Map.of(
                        "email",
                        email,
                        "password",
                        password
                    )
                );

        HttpRequest request =
            HttpRequest
                .newBuilder()
                .uri(
                    URI.create(
                        LOGIN_API
                    )
                )
                .header(
                    "Content-Type",
                    "application/json"
                )
                .header(
                    "Accept",
                    "application/json"
                )
                .POST(
                    HttpRequest
                        .BodyPublishers
                        .ofString(
                            body,
                            StandardCharsets.UTF_8
                        )
                )
                .build();

        HttpResponse<String> response =
            HTTP_CLIENT.send(
                request,
                HttpResponse
                    .BodyHandlers
                    .ofString()
            );

        if (
            response.statusCode()
            != 200
        ) {

            throw new IllegalStateException(
                "Simulator authentication failed with HTTP "
                + response.statusCode()
            );
        }

        JsonNode root =
            JSON_MAPPER
                .readTree(
                    response.body()
                );

        JsonNode tokenNode =
            root.get(
                "token"
            );

        if (
            tokenNode == null
            ||
            tokenNode.isNull()
            ||
            tokenNode
                .asString()
                .isBlank()
        ) {

            throw new IllegalStateException(
                "Authentication response does not contain a JWT token"
            );
        }

        System.out.println(
            "Simulator authentication successful."
        );

        return tokenNode
            .asString();
    }

    private static void
        publishTelemetry(
            MqttClient mqttClient,
            DeviceResponse device
        )
        throws Exception {

        TelemetryValues values =
            createTelemetry(
                device.type()
            );

        String payload =
            String.format(
                Locale.US,
                """
                {
                  "power": %.2f,
                  "voltage": %.2f,
                  "current": %.2f,
                  "temperature": %.2f
                }
                """,
                values.power(),
                values.voltage(),
                values.current(),
                values.temperature()
            );

        String topic =
            "enersense/devices/"
            + device.id()
            + "/telemetry";

        MqttMessage message =
            new MqttMessage(
                payload.getBytes(
                    StandardCharsets.UTF_8
                )
            );

        message.setQos(1);

        message.setRetained(
            false
        );

        mqttClient.publish(
            topic,
            message
        );

        System.out.printf(
            Locale.US,
            "Device %d | %-18s | %-12s | %.1f °C | %.0f W%n",
            device.id(),
            device.name(),
            device.type(),
            values.temperature(),
            values.power()
        );
    }

    private static TelemetryValues
        createTelemetry(
            DeviceType type
        ) {

        double voltage =
            randomBetween(
                225.0,
                235.0
            );

        double current;

        double temperature;

        double powerFactor;

        switch (type) {

            case BOILER -> {

                current =
                    randomBetween(
                        7.0,
                        10.0
                    );

                temperature =
                    randomBetween(
                        65.0,
                        90.0
                    );

                powerFactor =
                    0.90;
            }

            case PUMP -> {

                current =
                    randomBetween(
                        3.0,
                        8.0
                    );

                temperature =
                    randomBetween(
                        35.0,
                        70.0
                    );

                powerFactor =
                    0.86;
            }

            case FREEZER -> {

                current =
                    randomBetween(
                        2.0,
                        6.0
                    );

                temperature =
                    randomBetween(
                        -25.0,
                        -10.0
                    );

                powerFactor =
                    0.82;
            }

            case HVAC -> {

                current =
                    randomBetween(
                        4.0,
                        12.0
                    );

                temperature =
                    randomBetween(
                        18.0,
                        35.0
                    );

                powerFactor =
                    0.88;
            }

            case ENERGY_METER -> {

                current =
                    randomBetween(
                        1.0,
                        15.0
                    );

                temperature =
                    randomBetween(
                        20.0,
                        35.0
                    );

                powerFactor =
                    0.95;
            }

            default ->
                throw new IllegalArgumentException(
                    "Unsupported device type: "
                    + type
                );
        }

        double power =
            voltage
            * current
            * powerFactor;

        return new TelemetryValues(
            power,
            voltage,
            current,
            temperature
        );
    }

    private static double
        randomBetween(
            double min,
            double max
        ) {

        return ThreadLocalRandom
            .current()
            .nextDouble(
                min,
                max
            );
    }

    private static String
        requireEnvironmentVariable(
            String name
        ) {

        String value =
            System.getenv(
                name
            );

        if (
            value == null
            ||
            value.isBlank()
        ) {

            throw new IllegalStateException(
                "Missing environment variable: "
                + name
            );
        }

        return value;
    }

    private static void
        closeClient(
            MqttClient client
        ) {

        try {

            if (
                client.isConnected()
            ) {

                client.disconnect();
            }

            client.close();

        } catch (
            Exception exception
        ) {

            System.err.println(
                "Could not close MQTT client cleanly."
            );
        }
    }

    private record TelemetryValues(
        double power,
        double voltage,
        double current,
        double temperature
    ) {
    }
}