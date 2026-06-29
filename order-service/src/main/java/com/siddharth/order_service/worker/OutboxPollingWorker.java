package com.siddharth.order_service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siddharth.order_service.dto.OrderPlacedEvent;
import com.siddharth.order_service.model.OutboxMessage;
import com.siddharth.order_service.repository.OutboxRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OutboxPollingWorker {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObservationRegistry observationRegistry; // <--- INJECT THE REGISTRY

    @Scheduled(fixedDelay = 5000)
    public void processOutboxMessages() {

        // Wrap the execution inside an active Observation trace scope
        Observation.createNotStarted("outbox.polling.process", observationRegistry).observe(() -> {

            List<OutboxMessage> pendingMessages = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

            if (pendingMessages.isEmpty()) {
                return;
            }

            log.info("Found {} pending event(s) to publish.", pendingMessages.isEmpty() ? 0 : pendingMessages.size());

            for (OutboxMessage message : pendingMessages) {
                try {
                    OrderPlacedEvent event = objectMapper.readValue(message.getPayload(), OrderPlacedEvent.class);

                    // Because observation-enabled=true is in application.properties,
                    // this send call automatically grabs the trace context and injects it into Kafka headers!
                    kafkaTemplate.send(message.getTopic(), message.getAggregateId(), event).get();

                    message.setStatus("PROCESSED");
                    message.setProcessedAt(LocalDateTime.now());
                    outboxRepository.save(message);

                    log.info("Successfully exported message ID {} to Kafka broker.", message.getId());
                } catch (Exception e) {
                    log.error("Outbox Worker failed for message ID {}: {}", message.getId(), e.getMessage());
                    message.setStatus("FAILED");
                    outboxRepository.save(message);
                }
            }
        });
    }
}
