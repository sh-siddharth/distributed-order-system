package com.siddharth.inventory_service.dto;

import lombok.Data;

@Data
public class ReservationRequest {
    private String productId;
    private int quantity;
}
