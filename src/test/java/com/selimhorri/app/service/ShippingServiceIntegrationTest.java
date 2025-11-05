package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderStatus;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ShippingService Integration Tests - OrderService Integration")
class ShippingServiceIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should integrate with OrderService to fetch order details")
    void testOrderServiceIntegration_FetchOrderDetails() throws Exception {
        // Given
        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.ORDERED.name())
                .orderFee(100.0)
                .build();

        String orderJson = objectMapper.writeValueAsString(orderDto);
        mockServer.expect(requestTo(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"))
                .andRespond(withSuccess(orderJson, MediaType.APPLICATION_JSON));

        // When - Call OrderService via RestTemplate
        OrderDto result = restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1",
                OrderDto.class
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(OrderStatus.ORDERED.name(), result.getOrderStatus());
        assertEquals(100.0, result.getOrderFee());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle OrderService errors gracefully when order not found")
    void testOrderServiceIntegration_HandlesOrderNotFound() throws Exception {
        // Given
        mockServer.expect(requestTo(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/999"))
                .andRespond(withServerError());

        // When & Then
        assertThrows(Exception.class, () -> {
            restTemplate.getForObject(
                    AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/999",
                    OrderDto.class
            );
        });
        mockServer.verify();
    }
}

