package com.luxury.concierge.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Ambient intelligence event listener for concierge domain events.
 *
 * <p>Processes events asynchronously to avoid blocking the main web thread.
 * In production, this would invoke LangChain4j services to parse concierge
 * notes, update taste vectors, and generate proactive recommendations.</p>
 */
@Slf4j
@Component
public class ConciergeEventListener {

    /**
     * Handles concierge request events asynchronously.
     *
     * <p>In production, this method would:
     * <ul>
     *   <li>Parse concierge notes using LangChain4j's ChatLanguageModel</li>
     *   <li>Update taste vectors via EmbeddingModel</li>
     *   <li>Generate proactive recommendations based on request type</li>
     * </ul>
     * For local development, it logs the event details.</p>
     *
     * @param event the concierge request lifecycle event
     */
    @Async
    @EventListener
    public void handleConciergeRequestEvent(ConciergeRequestEvent event) {
        log.info("Ambient Intelligence — Processing {} event for request '{}' (type: {}, profile: {})",
                event.getEventType(),
                event.getRequest().getNumber(),
                event.getRequest().getRequestType(),
                event.getRequest().getProfileId());

        switch (event.getEventType()) {
            case CREATED -> log.debug("Queuing LangChain4j note parsing for new request: {}", event.getRequest().getNumber());
            case FULFILLED -> log.debug("Generating taste vector update from fulfilled request: {}", event.getRequest().getNumber());
            case CANCELLED -> log.debug("Recording cancellation patterns for predictive analytics: {}", event.getRequest().getNumber());
            case UPDATED -> log.debug("Re-evaluating ambient signals for updated request: {}", event.getRequest().getNumber());
        }
    }
}
