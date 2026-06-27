package com.siddharth.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private String aggregateId; // Stores the Order ID for tracing/routing keys

    @Column(columnDefinition = "TEXT")
    private String payload; // Stores the serialized JSON representation of our Event DTO

    private String status; // PENDING, PROCESSED, FAILED

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
