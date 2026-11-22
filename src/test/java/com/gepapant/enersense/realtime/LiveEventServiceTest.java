package com.gepapant.enersense.realtime;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class LiveEventServiceTest {

    @Test
    void subscribeShouldCreateEmitterAndRegisterCallbacks()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            SseEmitter result =
                    service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            assertSame(
                    emitter,
                    result
            );

            verify(emitter)
                    .onCompletion(
                            any(Runnable.class)
                    );

            verify(emitter)
                    .onTimeout(
                            any(Runnable.class)
                    );

            verify(emitter)
                    .onError(
                            any()
                    );

            verify(emitter)
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }

    @Test
    void sendTelemetryShouldBroadcastToSubscriber()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            clearInvocations(emitter);

            Object telemetry =
                    new Object();

            service.sendTelemetry(
                    telemetry
            );

            verify(emitter)
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }

    @Test
    void sendAlertShouldBroadcastToSubscriber()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            clearInvocations(emitter);

            Object alert =
                    new Object();

            service.sendAlert(
                    alert
            );

            verify(emitter)
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }

    @Test
    void failedBroadcastShouldRemoveEmitter()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            clearInvocations(emitter);

            doThrow(
                    new IOException(
                            "Connection closed"
                    )
            )
                    .when(emitter)
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );

            service.sendTelemetry(
                    new Object()
            );

            verify(emitter)
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );

            service.sendAlert(
                    new Object()
            );

            verify(emitter, times(1))
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }

    @Test
    void completionShouldRemoveEmitter()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            ArgumentCaptor<Runnable>
                    completionCaptor =
                    ArgumentCaptor.forClass(
                            Runnable.class
                    );

            verify(emitter)
                    .onCompletion(
                            completionCaptor.capture()
                    );

            completionCaptor
                    .getValue()
                    .run();

            clearInvocations(emitter);

            service.sendTelemetry(
                    new Object()
            );

            verify(
                    emitter,
                    never()
            )
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }

    @Test
    void timeoutShouldRemoveEmitter()
            throws Exception {

        try (
                MockedConstruction<SseEmitter> mocked =
                        mockConstruction(
                                SseEmitter.class
                        )
        ) {

            LiveEventService service =
                    new LiveEventService();

            service.subscribe();

            SseEmitter emitter =
                    mocked.constructed().get(0);

            ArgumentCaptor<Runnable>
                    timeoutCaptor =
                    ArgumentCaptor.forClass(
                            Runnable.class
                    );

            verify(emitter)
                    .onTimeout(
                            timeoutCaptor.capture()
                    );

            timeoutCaptor
                    .getValue()
                    .run();

            clearInvocations(emitter);

            service.sendAlert(
                    new Object()
            );

            verify(
                    emitter,
                    never()
            )
                    .send(
                            any(
                                    SseEmitter.SseEventBuilder.class
                            )
                    );
        }
    }
}