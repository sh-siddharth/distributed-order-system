package com.siddharth.order_service.consumer;

import com.siddharth.order_service.dto.InventoryProcessedEvent;
import com.siddharth.order_service.model.Order;
import com.siddharth.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryResponseConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "inventory-response-topic", groupId = "order-group")
    public void consumeInventoryResponse(InventoryProcessedEvent event) {
        System.out.println("\n<<< Kafka Consumer (Order): Received Inventory Response for Order: " + event.getOrderId());

        // 1. Fetch the existing local record from DB
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // 2. Evaluate Async Response and trigger State Machine
            if (event.isSuccess()) {
                System.out.println("SAGA STEP SUCCESS: Advancing state machine to INVENTORY_RESERVED...");
                order.reserveInventory(); // State transitions: PLACED -> INVENTORY_RESERVED
                orderRepository.save(order);
                System.out.println("Order status updated successfully in DB: " + order.getCurrentStatus());

                // --- COMPLETE THE LOOP: ADVANCE TO CONFIRMED ---
                System.out.println("SAGA STEP SUCCESS: Confirming payment and completing order...");
                order.confirmPayment(); // State transitions: INVENTORY_RESERVED -> CONFIRMED
                orderRepository.save(order);
                System.out.println("SAGA COMPLETE! Final Order status in DB: " + order.getCurrentStatus());
            } else {
                System.err.println("SAGA STEP FAILED: Rolling back order. Reason: " + event.getReason());
                order.cancel(event.getReason()); // State transitions: PLACED -> CANCELLED
                orderRepository.save(order);
                System.err.println("Order marked as CANCELLED in DB.");
            }
        } else {
            System.err.println("CRITICAL ERROR: Received inventory status for non-existent Order ID: " + event.getOrderId());
        }
    }
}
