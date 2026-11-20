package com.example.ledger.outbox;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record OutboxMessage(
        UUID id,
        String topic,
        String messageKey,
        String payloadJson,
        Instant createdAt,
        Optional<Instant> publishedAt

) {

}
