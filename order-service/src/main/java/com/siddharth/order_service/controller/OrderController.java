package com.siddharth.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siddharth.order_service.dto.OrderPlacedEvent;
import com.siddharth.order_service.dto.OrderRequest;
import com.siddharth.order_service.model.Order;
import com.siddharth.order_service.model.OutboxMessage;
import com.siddharth.order_service.repository.OrderRepository;
import com.siddharth.order_service.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    @Transactional // Ensures order creation and outbox logging happen atomically
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request) {

        log.info("Incoming REST request to create order for product: {}, quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            // 1. Create and Save local order with a unique UUID
            String uniqueOrderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Order order = new Order(uniqueOrderId, request.getProductId(), request.getQuantity(), request.getPrice());
            orderRepository.save(order);
            log.info("Step 1: Local Order [{}] saved safely to database.", uniqueOrderId);

            // 2. Map to Event Data Object
            OrderPlacedEvent event = new OrderPlacedEvent(
                    order.getId(),
                    order.getProductId(),
                    order.getQuantity(),
                    order.getPrice()
            );

            // 3. Serialize Event to JSON
            String jsonPayload = objectMapper.writeValueAsString(event);

            // 4. Stage inside the Outbox Table in the SAME transactional block
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setTopic("order-placed-topic");
            outboxMessage.setAggregateId(order.getId());
            outboxMessage.setPayload(jsonPayload);
            outboxMessage.setStatus("PENDING");
            outboxMessage.setCreatedAt(LocalDateTime.now());

            outboxRepository.save(outboxMessage);
            log.info("Step 2: Event serialized and staged securely in Outbox database table.");

            return ResponseEntity.ok("Order initiated successfully with ID: " + uniqueOrderId);

        } catch (Exception e) {
            log.error("CRITICAL: Failed to execute atomic Outbox transaction", e);
            return ResponseEntity.status(500).body("Order creation failed: " + e.getMessage());
        }
    }
}
