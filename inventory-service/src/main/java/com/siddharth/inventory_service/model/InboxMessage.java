package com.siddharth.inventory_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboxMessage {
    @Id
    @Column(name = "message_id") // Maps to the unique business identifier (e.g., Order ID)
    private String messageId;

    private String status; // PROCESSED

    private LocalDateTime processedAt;
}
