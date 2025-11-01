package com.example.demo.request.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @NotNull
    private Long id;
    private String name;
    private Long quantity;
}