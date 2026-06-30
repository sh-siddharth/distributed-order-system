package com.siddharth.inventory_service.repository;

import com.siddharth.inventory_service.model.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessage, String> {
}
