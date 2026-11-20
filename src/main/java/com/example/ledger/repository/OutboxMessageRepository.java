package com.example.ledger.repository;

import com.example.ledger.outbox.OutboxMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository {
    List<OutboxMessage> findUnpublished(int limit);
    void markPublished(UUID messageId, Instant publishedAt);
}
