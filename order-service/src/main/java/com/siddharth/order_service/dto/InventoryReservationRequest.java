package com.siddharth.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReservationRequest {
    private String productId;
    private int quantity;
}
