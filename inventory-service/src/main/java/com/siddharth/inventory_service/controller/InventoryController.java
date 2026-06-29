package com.siddharth.inventory_service.controller;

import com.siddharth.inventory_service.dto.ReservationRequest;
import com.siddharth.inventory_service.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveStock(@RequestBody ReservationRequest request) {
        // 3. USE 'log' TO PRINT TRACE CONTEXTS INFO
        log.info("Incoming REST request to reserve stock for product: {}, quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            inventoryService.reserveStock(request.getProductId(), request.getQuantity());

            log.info("Successfully reserved stock for product: {}", request.getProductId());
            return ResponseEntity.ok("Stock reserved successfully.");
        } catch (IllegalArgumentException e) {
            log.error("Validation failed while reserving stock: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Business rule violation while reserving stock: {}", e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            log.error("Server error encountered during stock reservation", e);
            return ResponseEntity.status(500).body("Concurrency failure or server error: " + e.getMessage());
        }
    }
}
