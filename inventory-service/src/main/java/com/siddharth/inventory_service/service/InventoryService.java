package com.siddharth.inventory_service.service;

import com.siddharth.inventory_service.model.Inventory;
import com.siddharth.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public void reserveStock(String productId, int quantity) {
        // 1. Fetch current stock state
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in stock ledger: " + productId));

        // 2. Business validation check
        if (inventory.getAvailableStock() < quantity) {
            throw new IllegalStateException("Insufficient stock available for Product: " + productId);
        }

        // 3. Deduct stock
        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);

        try {
            // 4. Save updates back to database
            inventoryRepository.save(inventory);
            System.out.println("Successfully reserved " + quantity + " units for product " + productId);
        } catch (ObjectOptimisticLockingFailureException ex) {
            // This catches the exact moment two concurrent transactions collide!
            System.err.println("ALERT: Race condition detected! Another transaction updated product " + productId + " simultaneously.");
            throw new RuntimeException("Failed to reserve stock due to concurrent activity. Please try again.");
        }
    }
}
