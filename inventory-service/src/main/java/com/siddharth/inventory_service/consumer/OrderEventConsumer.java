package com.siddharth.inventory_service.consumer;

import com.siddharth.inventory_service.dto.InventoryProcessedEvent;
import com.siddharth.inventory_service.dto.OrderPlacedEvent;
import com.siddharth.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, InventoryProcessedEvent> kafkaTemplate;

    private static final String RESPONSE_TOPIC = "inventory-response-topic";

    @KafkaListener(topics = "order-placed-topic", groupId = "inventory-group")
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        System.out.println("\n<<< Kafka Consumer: Received Event for Order ID: " + event.getOrderId());

        InventoryProcessedEvent responseEvent = new InventoryProcessedEvent();
        responseEvent.setOrderId(event.getOrderId());

        try {
            // Attempt to allocate stock
            inventoryService.reserveStock(event.getProductId(), event.getQuantity());

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
