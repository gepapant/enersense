package com.gepapant.enersense.realtime;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class LiveEventService {

    private final List<SseEmitter> emitters =
        new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {

        SseEmitter emitter =
            new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(
            () -> emitters.remove(emitter)
        );

        emitter.onTimeout(
            () -> emitters.remove(emitter)
        );

        emitter.onError(
            error -> emitters.remove(emitter)
        );

        try {
            emitter.send(
                SseEmitter
                    .event()
                    .name("connected")
                    .data("EnerSense live stream connected")
            );
        } catch (IOException exception) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void sendTelemetry(
        Object telemetry
    ) {
        broadcast(
            "telemetry",
            telemetry
        );
    }

    public void sendAlert(
        Object alert
    ) {
        broadcast(
            "alert",
            alert
        );
    }

    private void broadcast(
        String eventName,
        Object data
    ) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                    SseEmitter
                        .event()
                        .name(eventName)
                        .data(data)
                );

            } catch (IOException exception) {
                emitters.remove(emitter);
            }
        }
    }
}