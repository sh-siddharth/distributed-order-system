package com.siddharth.order_service.state;

import com.siddharth.order_service.model.Order;

public interface OrderState {

    void reserveInventory(Order order);
    void confirmPayment(Order order);
    void cancelOrder(Order order, String reason);
    OrderStatus getStatus();
}
