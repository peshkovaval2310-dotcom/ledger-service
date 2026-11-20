package com.example.ledger.kafka;

public interface KafkaProducer {
    void send(String topic, String key, String payloadJson);
}
