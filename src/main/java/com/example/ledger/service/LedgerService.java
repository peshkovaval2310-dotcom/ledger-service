package com.example.ledger.service;

import com.example.ledger.model.Application;
import com.example.ledger.model.ApplicationStatus;
import com.example.ledger.outbox.OutboxMessage;
import com.example.ledger.outbox.ApplicationOutboxStore;
import com.example.ledger.repository.ApplicationRepository;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class LedgerService implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final LenderClient lenderClient;
    private final ApplicationOutboxStore applicationOutboxStore;

    public LedgerService(ApplicationRepository applicationRepository,
                         LenderClient lenderClient,
                         ApplicationOutboxStore applicationOutboxStore) {
        this.applicationRepository= applicationRepository;
        this.lenderClient = lenderClient;
        this.applicationOutboxStore = applicationOutboxStore;
    }

    @Override
    public void blockLenderLimit(UUID id){
        Application application = applicationRepository.findById(id)
                .orElseThrow(()->
                        new NoSuchElementException("Заявка не найдена: " + id));
        if(application.status() != ApplicationStatus.SCORING_APPROVED){
            throw new IllegalStateException("Ожидался статус SCORING_APPROVED, получен: "
                    + application.status());
        }
        String requestID = "block-limit" + application.id();
        LenderBlockResult blockResult =lenderClient.blockLimit(
                application.lenderId(),
                application.id(),
                application.requestedAmount(),
                requestID

        );
        Application updateApplication = new Application(
                application.id(),
                application.clientId(),
                application.lenderId(),
                ApplicationStatus.LIMIT_BLOCKED,
                application.requestedAmount(),
                Optional.of(blockResult.blockId())
        );
        applicationRepository.save(updateApplication);

        String applicationId = updateApplication.id().toString();
        String payloadJson = """
              {
              "type": "ApplicationLimitBlocked",
              "applicationId": "%s"
            }
            """.formatted(applicationId);
        OutboxMessage message = new OutboxMessage(
                UUID.randomUUID(),
                "application-events",
                applicationId,
                payloadJson,
                Instant.now(),
                Optional.empty()

        );
        applicationOutboxStore.saveTogether(updateApplication, message);

    }
}
