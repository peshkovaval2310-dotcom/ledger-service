package com.example.ledger.outbox;

import com.example.ledger.kafka.KafkaProducer;
import com.example.ledger.repository.OutboxMessageRepository;

import java.time.Instant;

public class OutboxMessageProducer {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaProducer kafkaProducer;


    public OutboxMessageProducer(OutboxMessageRepository outboxMessageRepository, KafkaProducer kafkaProducer) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public void publishPending(){
        for(OutboxMessage message : outboxMessageRepository.findUnpublished(100)){
            kafkaProducer.send(
                    message.topic(),
                    message.messageKey(),
                    message.payloadJson()
            );
            outboxMessageRepository.markPublished(message.id(), Instant.now());
        }
    }

}
