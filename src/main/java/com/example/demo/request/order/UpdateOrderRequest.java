package com.example.demo.request.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class UpdateOrderRequest {

    @NotNull
    private Long id;

    @NotEmpty
    private String product;

    private Long ownerId;
}
