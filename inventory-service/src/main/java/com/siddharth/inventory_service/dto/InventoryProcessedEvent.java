package com.siddharth.inventory_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryProcessedEvent {
    private String orderId;
    private boolean success;
    private String reason;
}
