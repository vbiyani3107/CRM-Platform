package com.luxury.concierge.event;

import com.luxury.concierge.model.ExperientialRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event fired when an experiential request changes state.
 *
 * <p>Consumed by ambient intelligence listeners for LangChain4j
 * processing (e.g., parsing concierge notes, updating taste vectors).</p>
 */
@Getter
public class ConciergeRequestEvent extends ApplicationEvent {

    private final ExperientialRequest request;
    private final EventType eventType;

    public ConciergeRequestEvent(Object source, ExperientialRequest request, EventType eventType) {
        super(source);
        this.request = request;
        this.eventType = eventType;
    }

    /**
     * Types of concierge request lifecycle events.
     */
    public enum EventType {
        CREATED,
        UPDATED,
        FULFILLED,
        CANCELLED
    }
}
