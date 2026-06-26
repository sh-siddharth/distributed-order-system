package com.siddharth.order_service.model;

import com.siddharth.order_service.converter.OrderStateConverter;
import com.siddharth.order_service.state.OrderStatus;
import com.siddharth.order_service.state.OrderState;
import com.siddharth.order_service.state.PlacedState;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor // Required by JPA
public class Order {

    @Id
    private String id;
    private String productId;
    private int quantity;
    private double price;

    // The current active state instance (not persistent in DB directly as an object)
    @Column(name = "status")
    @Convert(converter = OrderStateConverter.class) // Links our LLD logic to the DB column!
    private OrderState orderState;

    public Order(String id, String productId, int quantity, double price) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.orderState = new PlacedState(); // Default initial state
    }

    // Delegate methods directly to the active state behavior
    public void reserveInventory() {
        this.orderState.reserveInventory(this);
    }

    public void confirmPayment() {
        this.orderState.confirmPayment(this);
    }

    public void cancel(String reason) {
        this.orderState.cancelOrder(this, reason);
    }

    // Helper to view current Enum status
    public OrderStatus getCurrentStatus() {
        return this.orderState.getStatus();
    }

}
