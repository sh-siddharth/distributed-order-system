package com.siddharth.order_service.state;

import com.siddharth.order_service.model.Order;

public class ConfirmedState implements OrderState{

    @Override
    public void reserveInventory(Order order) {
        throw new IllegalStateException("Order already completed!");
    }

    @Override
    public void confirmPayment(Order order) {
        throw new IllegalStateException("Payment already processed!");
    }

    @Override
    public void cancelOrder(Order order, String reason) {
        System.out.println("Cannot cancel a fully confirmed order!");
        throw new IllegalStateException("Cannot cancel a fully confirmed order!");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CONFIRMED;
    }
}
