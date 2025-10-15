package com.firefly.core.data.template.interfaces.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrderDTO {
    private String orderId;
    private String customerId;
    private Double amount;
    private String status;
    private Instant orderDate;
}