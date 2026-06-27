package com.siddharth.order_service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siddharth.order_service.dto.OrderPlacedEvent;
import com.siddharth.order_service.model.OutboxMessage;
import com.siddharth.order_service.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxPollingWorker {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate; // Use generic Object template

    @Autowired
    private ObjectMapper objectMapper;

    // Continuously scans the outbox every 5 seconds (5000ms) after the last execution completes
    @Scheduled(fixedDelay = 5000)
    public void processOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingMessages.isEmpty()) {
            return;
        }

        System.out.println(">>> Outbox Worker: Found " + pendingMessages.size() + " pending event(s) to publish.");

        for (OutboxMessage message : pendingMessages) {
            try {
                // 1. Reconstruct our strong Event DTO from JSON text
                OrderPlacedEvent event = objectMapper.readValue(message.getPayload(), OrderPlacedEvent.class);

                // 2. Publish to Kafka and wait for ACK
                kafkaTemplate.send(message.getTopic(), message.getAggregateId(), event).get();
                // Using .get() forces a synchronous block here so we confirm Kafka received it before moving on

                // 3. Update outbox record status on successful ACK
                message.setStatus("PROCESSED");
                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);

                System.out.println(">>> Outbox Worker: Successfully exported message ID " + message.getId() + " to Kafka.");
            } catch (Exception e) {
                System.err.println("CRITICAL: Outbox Worker failed to publish message ID " + message.getId() + ". Retrying next loop. Error: " + e.getMessage());

                message.setStatus("FAILED"); // You could add retry thresholds here for strict DLQ handling
                outboxRepository.save(message);
            }
        }
    }
}
