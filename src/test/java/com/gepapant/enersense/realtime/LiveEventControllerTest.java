package com.gepapant.enersense.realtime;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class LiveEventControllerTest {

    @Test
    void subscribeShouldDelegateToLiveEventService() {

        LiveEventService liveEventService =
                mock(
                        LiveEventService.class
                );

        SseEmitter emitter =
                mock(
                        SseEmitter.class
                );

        when(
                liveEventService.subscribe()
        )
                .thenReturn(
                        emitter
                );

        LiveEventController controller =
                new LiveEventController(
                        liveEventService
                );

        SseEmitter result =
                controller.subscribe();

        assertSame(
                emitter,
                result
        );

        verify(liveEventService)
                .subscribe();
    }
}