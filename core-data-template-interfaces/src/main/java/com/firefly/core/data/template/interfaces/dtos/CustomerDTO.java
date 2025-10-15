package com.firefly.core.data.template.interfaces.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDTO {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}