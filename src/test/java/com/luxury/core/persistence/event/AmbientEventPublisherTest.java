package com.luxury.core.persistence.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the ApplicationEventPublisher correctly fires and delivers
 * ambient intelligence events. This simulates the async event pattern that
 * LangChain4j processing will use.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ApplicationEventPublisher — Ambient Event Tests")
class AmbientEventPublisherTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TestEventListener testListener;

    /**
     * Domain event representing a client engagement being logged.
     * In production, the Ambient Intelligence service would consume this.
     */
    public record EngagementLoggedEvent(UUID profileId, String rawNotes) {}

    /**
     * Test listener that captures the event for assertion.
     */
    static class TestEventListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<EngagementLoggedEvent> captured = new AtomicReference<>();

        @EventListener
        public void handleEngagementLogged(EngagementLoggedEvent event) {
            captured.set(event);
            latch.countDown();
        }

        public EngagementLoggedEvent awaitEvent() throws InterruptedException {
            latch.await(5, TimeUnit.SECONDS);
            return captured.get();
        }
    }

    @TestConfiguration
    static class EventTestConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    @Test
    @DisplayName("Should publish and receive EngagementLoggedEvent")
    void shouldPublishAndReceiveEvent() throws InterruptedException {
        UUID profileId = UUID.randomUUID();
        String notes = "Client mentioned interest in minimalist platinum pieces during private dinner.";

        eventPublisher.publishEvent(new EngagementLoggedEvent(profileId, notes));

        EngagementLoggedEvent received = testListener.awaitEvent();

        assertThat(received).isNotNull();
        assertThat(received.profileId()).isEqualTo(profileId);
        assertThat(received.rawNotes()).contains("minimalist platinum");
    }
}
