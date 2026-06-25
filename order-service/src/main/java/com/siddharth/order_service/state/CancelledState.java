package com.siddharth.order_service.state;

import com.siddharth.order_service.model.Order;

public class CancelledState implements OrderState{

    @Override
    public void reserveInventory(Order order) {
        throw new IllegalStateException("Order is cancelled!");
    }

    @Override
    public void confirmPayment(Order order) {
        throw new IllegalStateException("Order is cancelled!");
    }

    @Override
    public void cancelOrder(Order order, String reason) {
        System.out.println("Order is already cancelled.");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
