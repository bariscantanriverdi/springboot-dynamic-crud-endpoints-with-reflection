package com.example.demo.request.product;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class CreateProductRequest {

    @NotEmpty
    private String name;
    private Long quantity;
}
