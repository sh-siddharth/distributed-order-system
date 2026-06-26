package com.siddharth.inventory_service.service;

import com.siddharth.inventory_service.model.Inventory;
import com.siddharth.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//@Component
public class InventorySimulationRunner implements CommandLineRunner {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== STARTING INVENTORY CONCURRENCY SIMULATION ===");

        // 1. Seed database
        String targetProductId = "IPHONE-18";
        inventoryRepository.save(new Inventory(targetProductId, 5, null));
        System.out.println("Seeded database: Product " + targetProductId + " has 5 units in stock.");

        // 2. Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);

        System.out.println("\n--- Launching 4 Concurrent Stock Reservation Requests ---");
        for (int i = 1; i <= 4; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    System.out.println("User " + userId + " is trying to reserve 2 units...");
                    inventoryService.reserveStock(targetProductId, 2);
                    System.out.println("SUCCESS: User " + userId + " secured stock!");
                } catch (Exception e) {
                    System.err.println("FAILURE for User " + userId + ": " + e.getMessage());
                }
            });
        }

        // 3. Alternative to awaitTermination: Initiate shutdown and sleep using primitive Thread sleep
        executor.shutdown();

        // Wait up to 5 seconds for threads to finish processing using standard sleep
        int retryCount = 0;
        while (!executor.isTerminated() && retryCount < 5) {
            Thread.sleep(1000); // Sleep for 1 second (1000 milliseconds)
            retryCount++;
        }

        // 4. Verify final database integrity
        Inventory finalInventory = inventoryRepository.findById(targetProductId).get();
        System.out.println("\n--- Final Integrity Check ---");
        System.out.println("Remaining Stock in DB: " + finalInventory.getAvailableStock());
        System.out.println("Final Database Row Version: " + finalInventory.getVersion());
        System.out.println("=== SIMULATION COMPLETED SUCCESSFULLY ===\n");
    }
}
