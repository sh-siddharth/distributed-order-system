package com.siddharth.order_service.state;

import com.siddharth.order_service.model.Order;
import lombok.Data;

@Data
public class InventoryReservedState implements OrderState {
    @Override
    public void reserveInventory(Order order) {
        throw new IllegalStateException("Inventory is already reserved for this order!");
    }

    @Override
    public void confirmPayment(Order order) {
        System.out.println("Payment confirmed for Order: " + order.getId());
        order.setOrderState(new ConfirmedState());
    }

    @Override
    public void cancelOrder(Order order, String reason) {
        System.out.println("Releasing inventory stock due to cancellation. Reason: " + reason);
        order.setOrderState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.INVENTORY_RESERVED;
    }
}
