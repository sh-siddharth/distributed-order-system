package com.siddharth.order_service.converter;

import com.siddharth.order_service.state.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStateConverter implements AttributeConverter<OrderState, String>{

    // Converts Java Object to Database Column String
    @Override
    public String convertToDatabaseColumn(OrderState attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getStatus().name();
    }

    // Converts Database Column String back to exact Java Object State Instance
    @Override
    public OrderState convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        OrderStatus status = OrderStatus.valueOf(dbData);
        return switch (status) {
            case PLACED -> new PlacedState();
            case INVENTORY_RESERVED -> new InventoryReservedState();
            case CONFIRMED -> new ConfirmedState();
            case CANCELLED -> new CancelledState();
        };
    }
}
