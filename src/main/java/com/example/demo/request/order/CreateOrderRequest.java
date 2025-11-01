package com.example.demo.request.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class CreateOrderRequest {

    @NotEmpty
    private String product;

    private Long ownerId;
}
