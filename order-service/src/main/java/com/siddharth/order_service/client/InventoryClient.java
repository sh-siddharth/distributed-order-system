package com.siddharth.order_service.client;

import com.siddharth.order_service.dto.InventoryReservationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    @Autowired
    private RestClient restClient;

    // Define the destination URI for our inventory microservice
    private final String inventoryServiceUrl = "http://localhost:8082/api/inventory/reserve";

    public boolean reserveInventory(String productId, int quantity) {
        try {
            InventoryReservationRequest requestBody = new InventoryReservationRequest(productId, quantity);

            restClient.post()
                    .uri(inventoryServiceUrl)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        // If inventory-service returns a 4xx or 5xx, handle the error gracefully
                        throw new RuntimeException("Inventory allocation rejected: " + response.getStatusCode());
                    })
                    .toBodilessEntity(); // We only care about the HTTP 200 OK status

            return true;
        } catch (Exception e) {
            System.err.println("Network Communication Failure to inventory-service: " + e.getMessage());
            return false;
        }
    }
}
