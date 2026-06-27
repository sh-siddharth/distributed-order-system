package com.siddharth.inventory_service.consumer;

import com.siddharth.inventory_service.dto.InventoryProcessedEvent;
import com.siddharth.inventory_service.dto.OrderPlacedEvent;
import com.siddharth.inventory_service.model.InboxMessage;
import com.siddharth.inventory_service.repository.InboxRepository;
import com.siddharth.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderEventConsumer {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, InventoryProcessedEvent> kafkaTemplate;

    @Autowired
    private InboxRepository inboxRepository;

    private static final String RESPONSE_TOPIC = "inventory-response-topic";

    @KafkaListener(topics = "order-placed-topic", groupId = "inventory-group")
    @Transactional // CRITICAL: Ensures Inbox record and Inventory deduction commit atomically
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        System.out.println("\n<<< Kafka Consumer: Received Event for Order ID: " + event.getOrderId());

        // 1. DEDUPLICATION CHECK (The Inbox Guard)
        if (inboxRepository.existsById(event.getOrderId())) {
            System.out.println("⚠️ IDEMPOTENCY WARNING: Order ID " + event.getOrderId() + " has already been processed! Skipping business logic to prevent duplicate stock deduction.");
            // Safe return: acknowledging the message back to Kafka without modifying stock again
            return;
        }

        InventoryProcessedEvent responseEvent = new InventoryProcessedEvent();
        responseEvent.setOrderId(event.getOrderId());

        try {
            // Attempt to allocate stock
            inventoryService.reserveStock(event.getProductId(), event.getQuantity());

            // 3. Register message inside the Inbox table within the SAME transaction
            InboxMessage inboxMessage = new InboxMessage(event.getOrderId(), "PROCESSED", LocalDateTime.now());
            inboxRepository.save(inboxMessage);
            System.out.println("✔ Inbox Guard: Message recorded successfully. Stock secured.");

            // If successful setup the success payload
            responseEvent.setSuccess(true);
            responseEvent.setReason("Stock allocated successfully");

        } catch (Exception e) {
            System.err.println("Failed to reserve stock asynchronously: " + e.getMessage());

            // If failed (e.g., out of stock), setup the failure payload
            responseEvent.setSuccess(false);
            responseEvent.setReason(e.getMessage());
        }

        // Broadcast the outcome back to Kafka!
        System.out.println(">>> Kafka Producer (Inventory): Broadcasting outcome back to order-service...");
        kafkaTemplate.send(RESPONSE_TOPIC, responseEvent.getOrderId(), responseEvent);
    }
}
