package com.siddharth.order_service;

import com.siddharth.order_service.model.Order;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StateMachineTestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING STATE MACHINE SIMULATION ===");

        // 1. Create a brand new order
        Order order = new Order("ORD-12345", "PROD-99", 2, 500.0);
        System.out.println("Initial Order Status: " + order.getCurrentStatus());

        // 2. Try an illegal action: processing payment right away
        try {
            System.out.println("\n--- Scenario A: Attempting an out-of-order action ---");
            order.confirmPayment();
        } catch (IllegalStateException e) {
            System.err.println("Caught Expected Rule Violation: " + e.getMessage());
        }

        // 3. Follow the happy path execution flow
        System.out.println("\n--- Scenario B: Executing the Happy Path ---");
        order.reserveInventory();
        System.out.println("Status after stock reservation: " + order.getCurrentStatus());

        order.confirmPayment();
        System.out.println("Status after payment: " + order.getCurrentStatus());

        // 4. Try to cancel a completed order
        try {
            System.out.println("\n--- Scenario C: Trying to cancel a completed order ---");
            order.cancel("Customer changed mind");
        } catch (IllegalStateException e) {
            System.err.println("Caught Expected Rule Violation: " + e.getMessage());
        }

        System.out.println("\n=== SIMULATION COMPLETED SUCCESSFULLY ===\n");
    }
}
