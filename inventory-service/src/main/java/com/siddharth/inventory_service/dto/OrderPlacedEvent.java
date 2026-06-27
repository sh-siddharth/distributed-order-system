package com.siddharth.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private String orderId;
    private String productId;
    private int quantity;
    private double totalPrice;
}
