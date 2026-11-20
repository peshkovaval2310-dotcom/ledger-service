package com.example.ledger;



import com.example.ledger.controller.ApplicationController;
import com.example.ledger.kafka.KafkaProducer;
import com.example.ledger.model.Application;
import com.example.ledger.model.ApplicationStatus;
import com.example.ledger.outbox.OutboxMessage;
import com.example.ledger.outbox.ApplicationOutboxStore;
import com.example.ledger.outbox.OutboxMessageProducer;
import com.example.ledger.repository.ApplicationRepository;
import com.example.ledger.repository.OutboxMessageRepository;
import com.example.ledger.service.ApplicationService;
import com.example.ledger.service.LedgerService;
import com.example.ledger.service.LenderBlockResult;
import com.example.ledger.service.LenderClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        Map<UUID, Application> applications = new LinkedHashMap<>();
        Map<UUID, OutboxMessage> outbox = new LinkedHashMap<>();

        UUID applicationId = UUID.randomUUID();

        Application application = new Application(
                applicationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ApplicationStatus.SCORING_APPROVED,
                new BigDecimal("100000.00"),
                Optional.empty()
        );

        applications.put(applicationId, application);

        ApplicationRepository applicationRepository = new ApplicationRepository() {
            @Override
            public Optional<Application> findById(UUID id) {
                return Optional.ofNullable(applications.get(id));
            }

            @Override
            public void save(Application application) {
                applications.put(application.id(), application);
            }
        };

        LenderClient lenderClient = (lenderId, id, amount, requestId) -> {
            System.out.printf(
                    "[Кредитор: демо] Заявка %s, сумма %s, запрос %s%n",
                    id, amount, requestId
            );

            return new LenderBlockResult("demo-block-1");
        };

        ApplicationOutboxStore applicationOutboxStore = (updatedApplication, message) -> {
            applicationRepository.save(updatedApplication);
            outbox.put(message.id(), message);
        };

        OutboxMessageRepository outboxRepository = new OutboxMessageRepository() {
            @Override
            public List<OutboxMessage> findUnpublished(int limit) {
                return outbox.values().stream()
                        .filter(message -> message.publishedAt().isEmpty())
                        .limit(limit)
                        .toList();
            }

            @Override
            public void markPublished(UUID messageId, Instant publishedAt) {
                OutboxMessage message = outbox.get(messageId);

                if (message == null || message.publishedAt().isPresent()) {
                    return;
                }

                OutboxMessage publishedMessage = new OutboxMessage(
                        message.id(),
                        message.topic(),
                        message.messageKey(),
                        message.payloadJson(),
                        message.createdAt(),
                        Optional.of(publishedAt)
                );

                outbox.put(messageId, publishedMessage);
            }
        };

        KafkaProducer kafkaProducer = (topic, key, payloadJson) -> {
            System.out.printf("[Kafka: демо] topic=%s, key=%s%n", topic, key);
            System.out.println(payloadJson);
        };

        ApplicationService applicationService = new LedgerService(
                applicationRepository,
                lenderClient,
                applicationOutboxStore
        );

        ApplicationController controller =
                new ApplicationController(applicationService);

        OutboxMessageProducer outboxProducer =
                new OutboxMessageProducer(outboxRepository, kafkaProducer);

        System.out.println("Статус до вызова: " + application.status());

        controller.blockLenderLimit(applicationId);

        Application updatedApplication =
                applicationRepository.findById(applicationId).orElseThrow();

        System.out.println("Статус после вызова: " + updatedApplication.status());
        System.out.println("ID резерва: " + updatedApplication.lenderBlockId().orElseThrow());
        System.out.println("Событий ждёт отправки: " + outboxRepository.findUnpublished(100).size());

        outboxProducer.publishPending();

        System.out.println("После публикации ждут отправки: "
                + outboxRepository.findUnpublished(100).size());
    }
}
