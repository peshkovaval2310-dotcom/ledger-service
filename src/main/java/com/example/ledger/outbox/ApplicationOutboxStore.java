package com.example.ledger.outbox;

import com.example.ledger.model.Application;

public interface ApplicationOutboxStore {
    void saveTogether(Application application, OutboxMessage outboxMessage);
}
