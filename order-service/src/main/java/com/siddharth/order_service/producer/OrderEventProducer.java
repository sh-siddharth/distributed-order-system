package com.siddharth.order_service.producer;

import com.siddharth.order_service.dto.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final String TOPIC = "order-placed-topic";

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlacedEvent(OrderPlacedEvent event) {
        System.out.println(">>> Kafka Producer: Publishing event for Order: " + event.getOrderId());

        // Send the event message asynchronously over the network to the broker
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }
}
