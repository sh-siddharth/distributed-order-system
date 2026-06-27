package com.siddharth.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siddharth.order_service.client.InventoryClient;
import com.siddharth.order_service.dto.OrderPlacedEvent;
import com.siddharth.order_service.model.Order;
import com.siddharth.order_service.model.OutboxMessage;
import com.siddharth.order_service.producer.OrderEventProducer;
import com.siddharth.order_service.repository.OrderRepository;
import com.siddharth.order_service.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class StateMachineTestRunner implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    /*@Autowired
    private InventoryClient inventoryClient;*/

    /*@Autowired
    private OrderEventProducer orderEventProducer;*/

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot automatically provides this bean

    /*@Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING DISTRIBUTED NETWORK TRANSACTION ===");

        // 1. Initialize a new order locally for our seeded product "PROD-100"
        Order order = new Order("ORD-NETWORK-TEST", "PROD-100", 2, 250.0);
        orderRepository.save(order);
        System.out.println("Step 1: Created local Order with Status: " + order.getCurrentStatus());

        // 2. Perform Network Call to Inventory-Service
        System.out.println("\nStep 2: Dispatching synchronous REST call to inventory-service...");
        boolean isStockSecured = inventoryClient.reserveInventory(order.getProductId(), order.getQuantity());

        // 3. Evaluate Network Outcome and Transition State
        if (isStockSecured) {
            System.out.println("Step 3: Network reservation successful! Updating State Machine...");
            order.reserveInventory(); // Moves state from PLACED -> INVENTORY_RESERVED
            orderRepository.save(order);
            System.out.println("Final Order Status in DB: " + order.getCurrentStatus());
        } else {
            System.err.println("Step 3: Stock reservation failed. Cancelling order...");
            order.cancel("Out of stock or communication breakdown");
            orderRepository.save(order);
        }

        System.out.println("\n=== DISTRIBUTED TRANSACTION TEST COMPLETE ===\n");
    }*/

    /*@Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING ASYNCHRONOUS EVENT TRANSACTION ===");

        // 1. Create local order
        Order order = new Order("ORD-KAFKA-ASYNC", "PROD-100", 2, 250.0);
        orderRepository.save(order);
        System.out.println("Step 1: Local Order saved with Status: " + order.getCurrentStatus());

        // 2. Map to Event DTO
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getPrice()
        );

        // 3. Fire-and-Forget: Publish the event to Kafka and complete our thread immediately
        System.out.println("Step 2: Broadcasting event to Kafka broker...");
        orderEventProducer.publishOrderPlacedEvent(event);

        System.out.println("=== ORDER-SERVICE TRANSACTION HANDOFF COMPLETE ===\n");
    }*/

    @Override
    @Transactional // CRITICAL: This ensures both inserts succeed together or roll back completely
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING ATOMIC OUTBOX TRANSACTION ===");

        // 1. Create and Save local order
        Order order = new Order("ORD-OUTBOX-TEST", "PROD-100", 2, 250.0);
        orderRepository.save(order);
        System.out.println("Step 1: Local Order saved safely to DB.");

        // 2. Map to Event DTO
        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getPrice()
        );

        // 3. Serialize Event Object to JSON String
        String jsonPayload = objectMapper.writeValueAsString(event);

        // 4. Save to Outbox Table in the SAME transaction
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setTopic("order-placed-topic");
        outboxMessage.setAggregateId(order.getId());
        outboxMessage.setPayload(jsonPayload);
        outboxMessage.setStatus("PENDING");
        outboxMessage.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(outboxMessage);
        System.out.println("Step 2: Event serialized and staged securely in Outbox table.");
        System.out.println("=== LOCAL ACID TRANSACTION COMMITTED SUCCESSFULLY ===\n");
    }
}
