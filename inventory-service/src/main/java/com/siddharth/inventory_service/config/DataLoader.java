package com.siddharth.inventory_service.config;

import com.siddharth.inventory_service.model.Inventory;
import com.siddharth.inventory_service.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(InventoryRepository repository) {
        return args -> {
            // Seed a product with 10 units in stock for our cross-service testing
            repository.save(new Inventory("PROD-100", 10, null));
            System.out.println("Inventory seeded: PROD-100 with 10 items.");
        };
    }
}
