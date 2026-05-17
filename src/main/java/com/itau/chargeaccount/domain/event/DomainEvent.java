package com.itau.chargeaccount.domain.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Implements the domain event pattern from DDD.
 */
@Getter
@RequiredArgsConstructor
public abstract class DomainEvent {
    private final String eventId = UUID.randomUUID().toString();
    private final LocalDateTime occurredAt = LocalDateTime.now();
    private final String eventType;

    /**
     * Returns the event type to be identified by consumers.
     */
    public abstract String getEventName();
}

