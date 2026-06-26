package com.siddharth.order_service;

import com.siddharth.order_service.model.Order;
import com.siddharth.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StateMachineTestRunner implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING PERSISTENCE DB TEST ===");

        // 1. Create and Save fresh order (Should be saved as 'PLACED' in DB)
        Order newOrder = new Order("ORD-999", "LAPTOP-01", 1, 1200.0);
        orderRepository.save(newOrder);
        System.out.println("Saved initial order to DB.");

        // 2. Read from DB and transition state
        Order savedOrder = orderRepository.findById("ORD-999")
                .orElseThrow(() -> new RuntimeException("Order not found"));
        System.out.println("Fetched from DB. Current polymorphic state class: "
                + savedOrder.getOrderState().getClass().getSimpleName());

        // Advance state
        savedOrder.reserveInventory();
        orderRepository.save(savedOrder); // Saves as 'INVENTORY_RESERVED'

        // 3. Verify final state extraction
        Order finalCheckOrder = orderRepository.findById("ORD-999").get();
        System.out.println("Final state verification from DB text string to Java object: "
                + finalCheckOrder.getCurrentStatus()); // Should print INVENTORY_RESERVED

        System.out.println("=== DB TEST COMPLETED SUCCESSFULLY ===\n");
    }
}
