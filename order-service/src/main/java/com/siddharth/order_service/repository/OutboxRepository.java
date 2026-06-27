package com.siddharth.order_service.repository;

import com.siddharth.order_service.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    // Fetch pending records sequentially for our polling worker to publish
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(String status);
}
