package com.siddharth.inventory_service.controller;

import com.siddharth.inventory_service.dto.ReservationRequest;
import com.siddharth.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveStock(@RequestBody ReservationRequest request) {
        try {
            inventoryService.reserveStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok("Stock reserved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Concurrency failure or server error: " + e.getMessage());
        }
    }
}
