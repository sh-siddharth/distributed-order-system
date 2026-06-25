package com.siddharth.order_service.state;

import com.siddharth.order_service.model.Order;

public class PlacedState implements OrderState{

    @Override
    public void reserveInventory(Order order) {
        System.out.println("Inventory reservation initiated for Order: " + order.getId());
        // Transition to next state
        order.setOrderState( new InventoryReservedState());
    }

    @Override
    public void confirmPayment(Order order) {
        System.out.println("Cannot confirm payment while order is in PLACED state. Inventory must be reserved first!");
        throw new IllegalStateException("Cannot confirm payment while order is in PLACED state. Inventory must be reserved first!");
    }

    @Override
    public void cancelOrder(Order order, String reason) {
        System.out.println("Order cancelled from PLACED state. Reason: " + reason);
        order.setOrderState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PLACED;
    }
}
